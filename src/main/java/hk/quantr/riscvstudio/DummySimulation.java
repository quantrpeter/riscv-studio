package hk.quantr.riscvstudio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * In-memory dummy CPU state so the windows look populated before the real
 * assembler and simulator are wired in.
 */
public final class DummySimulation {

	public static final String SOURCE_NAME = "riscv1.asm";

	public static final String SOURCE_TEXT = """
			# RISC-V Assembler & Simulator
			# Sample program: print a string via ecall

			        .section .text
			        .globl  _start

			_start:
			        j       main

			main:
			        la      a0, msg_hello
			        li      a7, 4
			        ecall
			        li      a0, 0
			        li      a7, 10
			        ecall

			loop:
			        j       loop

			        .section .data
			msg_hello:
			        .asciz  "Hello, RISC-V!"
			msg_exit:
			        .asciz  "Goodbye."
			""";

	public static final String[] ABI_NAMES = {
		"zero", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
		"s0/fp", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
		"a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
		"s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
	};

	private static final DummySimulation INSTANCE = new DummySimulation();

	private final List<Runnable> listeners = new ArrayList<>();
	private final List<TextRow> textSegment = new ArrayList<>();
	private final List<Breakpoint> breakpoints = new ArrayList<>();
	private final long[] registers = new long[32];
	private final long[] fregisters = new long[32];
	private final byte[] dataMemory = new byte[64];

	private long pc;
	private long nextPc;
	private long instructionCount;
	private long cycleCount;
	private long timeNs;
	private int speed = 40;
	private boolean running;
	private boolean assembled;
	private boolean halted;
	private String assemblerLog = "";
	private String linkerLog = "";
	private String ioOutput = "";
	private String status = "Ready";
	private final Timer runTimer;

	public static DummySimulation get() {
		return INSTANCE;
	}

	private DummySimulation() {
		runTimer = new Timer(220, e -> stepInternal());
		runTimer.setRepeats(true);
		resetState();
		seedProgram();
		assemblerLog = defaultAssemblerLog();
		linkerLog = defaultLinkerLog();
	}

	public synchronized void addListener(Runnable listener) {
		listeners.add(listener);
	}

	public synchronized void removeListener(Runnable listener) {
		listeners.remove(listener);
	}

	public void assemble() {
		resetState();
		seedProgram();
		assembled = true;
		status = "Build finished successfully.";
		assemblerLog = defaultAssemblerLog();
		linkerLog = defaultLinkerLog();
		appendIo("");
		fire();
	}

	public void reset() {
		boolean wasAssembled = assembled;
		resetState();
		seedProgram();
		assembled = wasAssembled;
		status = "Reset";
		fire();
	}

	public void run() {
		if (halted) {
			return;
		}
		running = true;
		status = "Running";
		runTimer.setDelay(speedToDelay());
		runTimer.start();
		fire();
	}

	public void pause() {
		running = false;
		runTimer.stop();
		status = halted ? "Halted" : "Paused";
		fire();
	}

	public void stepInto() {
		if (running) {
			pause();
		}
		stepInternal();
	}

	public void stepOver() {
		stepInto();
	}

	public void stepOut() {
		stepInto();
	}

	public void setSpeed(int value) {
		speed = Math.max(0, Math.min(100, value));
		runTimer.setDelay(speedToDelay());
		fire();
	}

	public void addBreakpoint(long address) {
		for (Breakpoint breakpoint : breakpoints) {
			if (breakpoint.address == address) {
				return;
			}
		}
		breakpoints.add(new Breakpoint(address, true));
		fire();
	}

	public void removeBreakpoint(int index) {
		if (index >= 0 && index < breakpoints.size()) {
			breakpoints.remove(index);
			fire();
		}
	}

	public void toggleBreakpoint(int index) {
		if (index >= 0 && index < breakpoints.size()) {
			Breakpoint breakpoint = breakpoints.get(index);
			breakpoint.enabled = !breakpoint.enabled;
			fire();
		}
	}

	public void clearIo() {
		ioOutput = "";
		fire();
	}

	public List<TextRow> getTextSegment() {
		return textSegment;
	}

	public List<Breakpoint> getBreakpoints() {
		return breakpoints;
	}

	public long[] getRegisters() {
		return registers;
	}

	public long[] getFregisters() {
		return fregisters;
	}

	public byte[] getDataMemory() {
		return dataMemory;
	}

	public long getPc() {
		return pc;
	}

	public long getNextPc() {
		return nextPc;
	}

	public long getInstructionCount() {
		return instructionCount;
	}

	public long getCycleCount() {
		return cycleCount;
	}

	public long getTimeNs() {
		return timeNs;
	}

	public int getSpeed() {
		return speed;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isAssembled() {
		return assembled;
	}

	public boolean isHalted() {
		return halted;
	}

	public String getAssemblerLog() {
		return assemblerLog;
	}

	public String getLinkerLog() {
		return linkerLog;
	}

	public String getIoOutput() {
		return ioOutput;
	}

	public String getStatus() {
		return status;
	}

	public int getPcRow() {
		for (int i = 0; i < textSegment.size(); i++) {
			if (textSegment.get(i).address == pc) {
				return i;
			}
		}
		return -1;
	}

	private void stepInternal() {
		if (halted || textSegment.isEmpty()) {
			pause();
			return;
		}
		int row = getPcRow();
		if (row < 0) {
			halted = true;
			running = false;
			runTimer.stop();
			status = "Halted";
			fire();
			return;
		}
		TextRow instruction = textSegment.get(row);
		apply(instruction);
		instructionCount++;
		cycleCount++;
		timeNs += 10;
		if (hitBreakpoint(nextPc) && running) {
			pc = nextPc;
			nextPc = pc + 4;
			running = false;
			runTimer.stop();
			status = String.format("Breakpoint at 0x%08X", pc);
			fire();
			return;
		}
		if (!halted) {
			pc = nextPc;
			nextPc = pc + 4;
			status = "Stepping";
		}
		fire();
	}

	private void apply(TextRow instruction) {
		switch (instruction.mnemonic) {
			case "j" -> {
				if ("main".equals(instruction.operand)) {
					nextPc = 0x00000004L;
				} else {
					nextPc = instruction.address;
					halted = true;
					running = false;
					runTimer.stop();
					status = "Halted";
				}
			}
			case "la" -> {
				registers[10] = 0x00000024L;
				nextPc = instruction.address + 8;
			}
			case "li" -> {
				if ("a7".equals(instruction.rd)) {
					registers[17] = instruction.imm;
				} else if ("a0".equals(instruction.rd)) {
					registers[10] = instruction.imm;
				}
				nextPc = instruction.address + 4;
			}
			case "ecall" -> {
				if (registers[17] == 4) {
					appendIo("Hello, RISC-V!\n");
				} else if (registers[17] == 10) {
					halted = true;
					running = false;
					runTimer.stop();
					status = "Program exited";
				}
				nextPc = instruction.address + 4;
			}
			default -> nextPc = instruction.address + 4;
		}
	}

	private boolean hitBreakpoint(long address) {
		for (Breakpoint breakpoint : breakpoints) {
			if (breakpoint.enabled && breakpoint.address == address) {
				return true;
			}
		}
		return false;
	}

	private void resetState() {
		running = false;
		halted = false;
		assembled = false;
		pc = 0;
		nextPc = 4;
		instructionCount = 0;
		cycleCount = 0;
		timeNs = 0;
		ioOutput = "";
		status = "Ready";
		runTimer.stop();
		Arrays.fill(registers, 0);
		Arrays.fill(fregisters, 0);
		Arrays.fill(dataMemory, (byte) 0);
		registers[2] = 0x7FFFFFF0L;
	}

	private void seedProgram() {
		textSegment.clear();
		textSegment.add(new TextRow(0x00000000L, 0x0040006F, "j main", "j       main", "j", "main", 0));
		textSegment.add(new TextRow(0x00000004L, 0x00000517, "auipc a0, 0x0", "la      a0, msg_hello", "la", "a0", 0));
		textSegment.add(new TextRow(0x00000008L, 0x02050513, "addi a0, a0, 32", "la      a0, msg_hello", "la", "a0", 0));
		textSegment.add(new TextRow(0x0000000CL, 0x00400893, "addi a7, zero, 4", "li      a7, 4", "li", "a7", 4));
		textSegment.add(new TextRow(0x00000010L, 0x00000073, "ecall", "ecall", "ecall", "", 0));
		textSegment.add(new TextRow(0x00000014L, 0x00000513, "addi a0, zero, 0", "li      a0, 0", "li", "a0", 0));
		textSegment.add(new TextRow(0x00000018L, 0x00A00893, "addi a7, zero, 10", "li      a7, 10", "li", "a7", 10));
		textSegment.add(new TextRow(0x0000001CL, 0x00000073, "ecall", "ecall", "ecall", "", 0));
		textSegment.add(new TextRow(0x00000020L, 0x0000006F, "j loop", "j       loop", "j", "loop", 0));

		writeString(0, "Hello, RISC-V!");
		writeString(15, "Goodbye.");

		breakpoints.clear();
		breakpoints.add(new Breakpoint(0x00000010L, true));
		breakpoints.add(new Breakpoint(0x00000024L, false));
	}

	private void writeString(int offset, String text) {
		byte[] bytes = text.getBytes();
		System.arraycopy(bytes, 0, dataMemory, offset, bytes.length);
		dataMemory[offset + bytes.length] = 0;
	}

	private String defaultAssemblerLog() {
		return """
				INFO  Assembling riscv1.asm
				INFO  Pass 1 complete
				INFO  Pass 2 complete
				INFO  Code size: 36 bytes
				INFO  Data size: 24 bytes
				INFO  0 error(s), 0 warning(s)
				Build finished successfully.
				""";
	}

	private String defaultLinkerLog() {
		return """
				INFO  Linking riscv1.o
				INFO  Section .text  0x00000000  36 bytes
				INFO  Section .data  0x00000024  24 bytes
				INFO  Entry point    0x00000000  _start
				INFO  0 error(s)
				Link finished successfully.
				""";
	}

	private void appendIo(String text) {
		ioOutput = text;
	}

	private int speedToDelay() {
		return Math.max(20, 420 - speed * 4);
	}

	private void fire() {
		List<Runnable> snapshot;
		synchronized (this) {
			snapshot = new ArrayList<>(listeners);
		}
		Runnable notify = () -> {
			for (Runnable listener : snapshot) {
				listener.run();
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			notify.run();
		} else {
			SwingUtilities.invokeLater(notify);
		}
	}

	public static final class TextRow {

		public final long address;
		public final int code;
		public final String disassembly;
		public final String source;
		public final String mnemonic;
		public final String rd;
		public final int imm;
		public final String operand;

		TextRow(long address, int code, String disassembly, String source, String mnemonic, String operand, int imm) {
			this.address = address;
			this.code = code;
			this.disassembly = disassembly;
			this.source = source;
			this.mnemonic = mnemonic;
			this.operand = operand;
			this.rd = operand;
			this.imm = imm;
		}
	}

	public static final class Breakpoint {

		public final long address;
		public boolean enabled;

		Breakpoint(long address, boolean enabled) {
			this.address = address;
			this.enabled = enabled;
		}
	}
}
