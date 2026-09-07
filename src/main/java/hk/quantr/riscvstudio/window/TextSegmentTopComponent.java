package hk.quantr.riscvstudio.window;

import hk.quantr.riscvstudio.DummySimulation;
import hk.quantr.riscvstudio.ui.TextSegmentPanel;
import java.awt.BorderLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

@ConvertAsProperties(
		dtd = "-//hk.quantr.riscvstudio.window//TextSegment//EN",
		autostore = false
)
@TopComponent.Description(
		preferredID = "TextSegmentTopComponent",
		persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "riscvTextSegment", openAtStartup = false)
@ActionID(category = "Window", id = "hk.quantr.riscvstudio.window.TextSegmentTopComponent")
@ActionReference(path = "Menu/Window/RISC-V", position = 300)
@TopComponent.OpenActionRegistration(
		displayName = "Text Segment",
		preferredID = "TextSegmentTopComponent"
)
public final class TextSegmentTopComponent extends TopComponent {

	private final TextSegmentPanel panel = new TextSegmentPanel();
	private final Runnable listener = panel::refresh;

	public TextSegmentTopComponent() {
		initComponents();
		setName("Text Segment");
		setToolTipText("Assembled instructions at the current PC");
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
