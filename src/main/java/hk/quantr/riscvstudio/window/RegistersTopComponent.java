package hk.quantr.riscvstudio.window;

import hk.quantr.riscvstudio.DummySimulation;
import hk.quantr.riscvstudio.ui.RegistersPanel;
import java.awt.BorderLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

@ConvertAsProperties(
		dtd = "-//hk.quantr.riscvstudio.window//Registers//EN",
		autostore = false
)
@TopComponent.Description(
		preferredID = "RegistersTopComponent",
		persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "riscvRegisters", openAtStartup = false)
@ActionID(category = "Window", id = "hk.quantr.riscvstudio.window.RegistersTopComponent")
@ActionReference(path = "Menu/Window/RISC-V", position = 500)
@TopComponent.OpenActionRegistration(
		displayName = "Registers",
		preferredID = "RegistersTopComponent"
)
public final class RegistersTopComponent extends TopComponent {

	private final RegistersPanel panel = new RegistersPanel();
	private final Runnable listener = panel::refresh;

	public RegistersTopComponent() {
		initComponents();
		setName("Registers");
		setToolTipText("Integer and floating-point registers");
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
