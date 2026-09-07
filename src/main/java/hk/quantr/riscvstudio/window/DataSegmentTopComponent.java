package hk.quantr.riscvstudio.window;

import hk.quantr.riscvstudio.DummySimulation;
import hk.quantr.riscvstudio.ui.DataSegmentPanel;
import java.awt.BorderLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

@ConvertAsProperties(
		dtd = "-//hk.quantr.riscvstudio.window//DataSegment//EN",
		autostore = false
)
@TopComponent.Description(
		preferredID = "DataSegmentTopComponent",
		persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "riscvDataSegment", openAtStartup = false)
@ActionID(category = "Window", id = "hk.quantr.riscvstudio.window.DataSegmentTopComponent")
@ActionReference(path = "Menu/Window/RISC-V", position = 400)
@TopComponent.OpenActionRegistration(
		displayName = "Data Segment",
		preferredID = "DataSegmentTopComponent"
)
public final class DataSegmentTopComponent extends TopComponent {

	private final DataSegmentPanel panel = new DataSegmentPanel();
	private final Runnable listener = panel::refresh;

	public DataSegmentTopComponent() {
		initComponents();
		setName("Data Segment");
		setToolTipText("Data memory dump");
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
