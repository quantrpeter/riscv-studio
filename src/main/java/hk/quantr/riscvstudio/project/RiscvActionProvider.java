package hk.quantr.riscvstudio.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Maps NetBeans Build / Clean / Run to the project Makefile.
 */
public final class RiscvActionProvider implements ActionProvider {

	public static final String MAKEFILE = "Makefile";

	private static final String[] SUPPORTED = {
		COMMAND_BUILD,
		COMMAND_CLEAN,
		COMMAND_REBUILD,
		COMMAND_RUN
	};

	private static final RequestProcessor RP = new RequestProcessor(RiscvActionProvider.class);

	private final RiscvProject project;

	RiscvActionProvider(RiscvProject project) {
		this.project = project;
	}

	@Override
	public String[] getSupportedActions() {
		return SUPPORTED.clone();
	}

	@Override
	public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
		return supports(command) && makefile() != null;
	}

	@Override
	public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
		if (!supports(command)) {
			throw new IllegalArgumentException(command);
		}
		LifecycleManager.getDefault().saveAll();
		String[] targets = targetsFor(command);
		ActionProgress progress = ActionProgress.start(context);
		RP.post(() -> {
			boolean success = false;
			try {
				success = runMake(targets);
			} finally {
				progress.finished(success);
			}
		});
	}

	static String[] targetsFor(String command) {
		switch (command) {
			case COMMAND_BUILD:
				return new String[]{"compile"};
			case COMMAND_CLEAN:
				return new String[]{"clean"};
			case COMMAND_REBUILD:
				return new String[]{"clean", "compile"};
			case COMMAND_RUN:
				return new String[]{"run"};
			default:
				throw new IllegalArgumentException(command);
		}
	}

	private boolean supports(String command) {
		for (String supported : SUPPORTED) {
			if (supported.equals(command)) {
				return true;
			}
		}
		return false;
	}

	private FileObject makefile() {
		FileObject file = project.getProjectDirectory().getFileObject(MAKEFILE);
		return file != null && file.isData() ? file : null;
	}

	private boolean runMake(String... targets) {
		File dir = FileUtil.toFile(project.getProjectDirectory());
		InputOutput io = IOProvider.getDefault().getIO("RISC-V Build", false);
		io.select();
		try {
			io.getOut().reset();
		} catch (IOException ignored) {
		}

		if (dir == null) {
			io.getErr().println("Project directory is not available on disk.");
			return false;
		}
		if (makefile() == null) {
			io.getErr().println("No Makefile in " + dir.getAbsolutePath());
			return false;
		}

		List<String> cmd = new ArrayList<>();
		cmd.add("make");
		cmd.addAll(Arrays.asList(targets));

		try (OutputWriter out = io.getOut(); OutputWriter err = io.getErr()) {
			out.println("cd " + dir.getAbsolutePath());
			out.println(String.join(" ", cmd));
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.directory(dir);
			pb.redirectErrorStream(true);
			Process process = pb.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				String line;
				while ((line = reader.readLine()) != null) {
					out.println(line);
				}
			}
			int exit = process.waitFor();
			FileUtil.refreshFor(dir);
			if (exit == 0) {
				out.println("BUILD SUCCESSFUL");
				return true;
			}
			err.println("BUILD FAILED (exit code " + exit + ")");
			return false;
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			io.getErr().println("Build interrupted.");
			return false;
		} catch (IOException ex) {
			io.getErr().println(ex.getMessage() != null ? ex.getMessage() : ex.toString());
			return false;
		}
	}
}
