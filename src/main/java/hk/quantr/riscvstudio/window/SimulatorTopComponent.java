package hk.quantr.riscvstudio.window;

import hk.quantr.riscvstudio.DummySimulation;
import hk.quantr.riscvstudio.ui.SimulatorPanel;
import java.awt.BorderLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

@ConvertAsProperties(
		dtd = "-//hk.quantr.riscvstudio.window//Simulator//EN",
		autostore = false
)
@TopComponent.Description(
		preferredID = "SimulatorTopComponent",
		persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "riscvSimulator", openAtStartup = false)
@ActionID(category = "Window", id = "hk.quantr.riscvstudio.window.SimulatorTopComponent")
@ActionReference(path = "Menu/Window/RISC-V", position = 100)
@TopComponent.OpenActionRegistration(
		displayName = "Simulator",
		preferredID = "SimulatorTopComponent"
)
public final class SimulatorTopComponent extends TopComponent {

	private final SimulatorPanel panel = new SimulatorPanel();
	private final Runnable listener = panel::refresh;

	public SimulatorTopComponent() {
		initComponents();
		setName("Simulator");
		setToolTipText("Assemble, run and step the RISC-V simulator");
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
	}

	@Override
	public void componentOpened() {
		DummySimulation.get().addListener(listener);
		panel.refresh();
	}

	@Override
	public void componentClosed() {
		DummySimulation.get().removeListener(listener);
	}

	void writeProperties(java.util.Properties p) {
		p.setProperty("version", "1.0");
	}

	void readProperties(java.util.Properties p) {
	}
}
