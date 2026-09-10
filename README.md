# RISC-V Studio

NetBeans module that turns the IDE into a RISC-V assembler and simulator.

![](image/preview.png)

```sh
mvn nbm:run
```

## Source Structure

Entry: `src/main/nbm/manifest.mf` → `layer.xml` + annotations.

```mermaid
%%{init: {"theme": "neutral", "flowchart": {"curve": "linear", "nodeSpacing": 14, "rankSpacing": 32, "padding": 8}}}%%
flowchart TB
  MF["ENTRY  manifest.mf"]

  MF -->|OpenIDE-Module-Layer| LY[layer.xml]
  MF -->|Localizing-Bundle| BP[Bundle.properties]
  MF -->|APT + Lookup| ANN["annotations → generated-layer.xml"]

  subgraph layerxml["layer.xml"]
    direction LR
    SIM_M[riscvSimulator.wsmode]
    TXT_M[riscvTextSegment.wsmode]
    DAT_M[riscvDataSegment.wsmode]
    REG_M[riscvRegisters.wsmode]
    BARS["Menu/RISC-V  ·  Menu/Window/RISC-V  ·  Toolbars/RISC-V"]
  end

  LY --> SIM_M
  LY --> TXT_M
  LY --> DAT_M
  LY --> REG_M
  LY --> BARS

  subgraph anno["annotations"]
    direction LR
    ATC["@TopComponent.Registration"]
    AACT["@ActionRegistration"]
    ASP["@ServiceProvider"]
    ATPL["@TemplateRegistration"]
    AON["@OnShowing"]
  end

  ANN --> ATC
  ANN --> AACT
  ANN --> ASP
  ANN --> ATPL
  ANN --> AON

  ATC --> STC[SimulatorTopComponent]
  ATC --> TTC[TextSegmentTopComponent]
  ATC --> DTC[DataSegmentTopComponent]
  ATC --> RTC[RegistersTopComponent]
  ATC --> MTC[MessagesTopComponent]
  AACT --> SA[SimulatorActions]
  ASP --> RF[RiscvProjectFactory]
  ATPL --> WI[RiscvProjectWizardIterator]
  AON --> START[OpenWindowsOnStart]

  SIM_M -->|mode| STC
  TXT_M -->|mode| TTC
  DAT_M -->|mode| DTC
  REG_M -->|mode| RTC
  BARS --> SA
  BARS --> MTC

  subgraph wizard["New Project — writes files only"]
    direction TB
    WP[RiscvProjectWizardPanel]
    VP[RiscvProjectVisualPanel]
    MARK[riscv.project]
    MAKE[Makefile]
    WI --> WP --> VP
    WI -->|writes| MARK
    WI -->|writes| MAKE
  end

  subgraph open["Open Project — constructs objects"]
    direction TB
    PJ[RiscvProject]
    LV[RiscvLogicalView]
    AP[RiscvActionProvider]
    RF -->|if marker exists| PJ
    PJ --> LV
    PJ --> AP
  end

  MARK -.->|later detected by| RF
  AP -->|runs make| MAKE

  subgraph ui["ui"]
    direction TB
    SP[SimulatorPanel]
    TP[TextSegmentPanel]
    DP[DataSegmentPanel]
    RP[RegistersPanel]
    MP[MessagesPanel]
  end

  STC --> SP
  TTC --> TP
  DTC --> DP
  RTC --> RP
  MTC --> MP

  subgraph shared["shared"]
    direction TB
    DS[DummySimulation]
    UD[UiDefaults]
  end

  SA --> DS
  ui --> DS
  ui --> UD

  style MF fill:#ffe082,stroke:#f9a825
  style LY fill:#bbdefb,stroke:#1565c0
  style layerxml fill:#e3f2fd,stroke:#1565c0
  style anno fill:#f3e5f5,stroke:#6a1b9a
  style wizard fill:#e8f5e9,stroke:#2e7d32
  style open fill:#c8e6c9,stroke:#2e7d32
  style ui fill:#fff8e1,stroke:#f9a825
  style shared fill:#fce4ec,stroke:#ad1457
```
