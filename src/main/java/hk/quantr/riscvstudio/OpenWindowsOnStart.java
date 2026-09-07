package hk.quantr.riscvstudio;

import org.openide.windows.OnShowing;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@OnShowing
public class OpenWindowsOnStart implements Runnable {

	@Override
	public void run() {
		WindowManager.getDefault().invokeWhenUIReady(() -> {
			TopComponent workspace = WindowManager.getDefault().findTopComponent("WorkspaceTopComponent");
			if (workspace != null) {
				workspace.open();
				workspace.requestActive();
			}
		});
	}
}
