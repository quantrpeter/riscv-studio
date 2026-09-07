package hk.quantr.riscvstudio.window;

import hk.quantr.riscvstudio.DummySimulation;
import hk.quantr.riscvstudio.ui.MessagesPanel;
import java.awt.BorderLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

@ConvertAsProperties(
		dtd = "-//hk.quantr.riscvstudio.window//Messages//EN",
		autostore = false
)
@TopComponent.Description(
		preferredID = "MessagesTopComponent",
		persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "hk.quantr.riscvstudio.window.MessagesTopComponent")
@ActionReference(path = "Menu/Window/RISC-V", position = 200)
@TopComponent.OpenActionRegistration(
		displayName = "Messages",
		preferredID = "MessagesTopComponent"
)
public final class MessagesTopComponent extends TopComponent {

	private final MessagesPanel panel = new MessagesPanel();
	private final Runnable listener = panel::refresh;

	public MessagesTopComponent() {
		initComponents();
		setName("Messages");
		setToolTipText("Assembler and linker output");
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
