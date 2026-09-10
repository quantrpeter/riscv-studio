# RISC-V Studio
# Assemble with quantr riscv-assembler, run with quantr riscv-simulator.
#
#   make          assemble src/*.asm
#   make run      assemble, then execute
#   make debug    assemble, then open the simulator prompt
#   make clean    remove build/

ARCH           ?= rv64
SRC_DIR        ?= src
BUILD_DIR      ?= build
TARGET         ?= $(notdir $(CURDIR))
SRC            ?= $(firstword $(wildcard $(SRC_DIR)/*.asm $(SRC_DIR)/*.s $(SRC_DIR)/*.S))

WORKSPACE      ?= $(HOME)/workspace
ASSEMBLER_HOME ?= $(WORKSPACE)/assembler
SIMULATOR_HOME ?= $(WORKSPACE)/riscv-simulator
ASSEMBLER_JAR  ?= $(firstword $(wildcard $(ASSEMBLER_HOME)/target/assembler-*.jar))
SIMULATOR_JAR  ?= $(firstword $(wildcard $(SIMULATOR_HOME)/target/riscv_simulator-*-jar-with-dependencies.jar $(SIMULATOR_HOME)/target/riscv_simulator-*.jar))

JAVA           ?= java
ASSEMBLER      ?= $(JAVA) -jar "$(ASSEMBLER_JAR)"
SIMULATOR      ?= $(JAVA) -jar "$(SIMULATOR_JAR)"

BIN            := $(BUILD_DIR)/$(TARGET).bin
ELF            := $(BUILD_DIR)/$(TARGET).elf
LST            := $(BUILD_DIR)/$(TARGET).lst

START          ?= 0x0
SETTING        ?= setting.xml
SIM_COMMANDS   ?= c

.PHONY: all compile run run-elf debug clean help
.DELETE_ON_ERROR:

all: compile

compile: $(BIN)

$(BUILD_DIR):
	mkdir -p "$(BUILD_DIR)"

$(BIN): $(SRC) | $(BUILD_DIR)
	@test -n "$(SRC)" || { echo "No assembly source found in $(SRC_DIR)"; exit 1; }
	@test -n "$(ASSEMBLER_JAR)" -a -f "$(ASSEMBLER_JAR)" || { echo "riscv-assembler jar not found. Build $(ASSEMBLER_HOME) or set ASSEMBLER_JAR"; exit 1; }
	$(ASSEMBLER) -a $(ARCH) -o "$@" -l "$(LST)" "$<"

$(ELF): $(SRC) | $(BUILD_DIR)
	@test -n "$(SRC)" || { echo "No assembly source found in $(SRC_DIR)"; exit 1; }
	@test -n "$(ASSEMBLER_JAR)" -a -f "$(ASSEMBLER_JAR)" || { echo "riscv-assembler jar not found. Build $(ASSEMBLER_HOME) or set ASSEMBLER_JAR"; exit 1; }
	$(ASSEMBLER) -a $(ARCH) -f elf -o "$@" -l "$(LST)" "$<"

run: $(BIN)
	@test -n "$(SIMULATOR_JAR)" -a -f "$(SIMULATOR_JAR)" || { echo "riscv-simulator jar not found. Build $(SIMULATOR_HOME) or set SIMULATOR_JAR"; exit 1; }
	@test -f "$(SETTING)" || { echo "missing $(SETTING) (simulator init file)"; exit 1; }
	$(SIMULATOR) --init "$(SETTING)" --binary "$(BIN),$(START)" -s $(START) --commands "$(SIM_COMMANDS)"

run-elf: $(ELF)
	@test -n "$(SIMULATOR_JAR)" -a -f "$(SIMULATOR_JAR)" || { echo "riscv-simulator jar not found. Build $(SIMULATOR_HOME) or set SIMULATOR_JAR"; exit 1; }
	@test -f "$(SETTING)" || { echo "missing $(SETTING) (simulator init file)"; exit 1; }
	$(SIMULATOR) --init "$(SETTING)" --elf "$(ELF)" -s $(START) --commands "$(SIM_COMMANDS)"

debug: $(BIN)
	@test -n "$(SIMULATOR_JAR)" -a -f "$(SIMULATOR_JAR)" || { echo "riscv-simulator jar not found. Build $(SIMULATOR_HOME) or set SIMULATOR_JAR"; exit 1; }
	@test -f "$(SETTING)" || { echo "missing $(SETTING) (simulator init file)"; exit 1; }
	$(SIMULATOR) --init "$(SETTING)" --binary "$(BIN),$(START)" -s $(START)

clean:
	rm -rf "$(BUILD_DIR)"

help:
	@echo "make compile   assemble $(SRC) with riscv-assembler"
	@echo "make run       assemble and execute with riscv-simulator"
	@echo "make run-elf   assemble to ELF and execute"
	@echo "make debug     assemble and open the simulator prompt"
	@echo "make clean     remove $(BUILD_DIR)"
	@echo
	@echo "Override ASSEMBLER_JAR, SIMULATOR_JAR, ARCH, SRC, START, SETTING, SIM_COMMANDS as needed."
