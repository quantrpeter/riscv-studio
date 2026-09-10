package hk.quantr.riscvstudio.project;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class RiscvLogicalView implements LogicalViewProvider {

	private final RiscvProject project;

	public RiscvLogicalView(RiscvProject project) {
		this.project = project;
	}

	@Override
	public Node createLogicalView() {
		return new ProjectNode(project);
	}

	@Override
	public Node findPath(Node root, Object target) {
		return null;
	}

	private static final class ProjectNode extends AbstractNode {

		ProjectNode(RiscvProject project) {
			super(new ProjectChildren(project), Lookups.fixed(project, project.getProjectDirectory()));
			setName(project.getProjectDirectory().getName());
			setDisplayName(project.getProjectDirectory().getName());
			setShortDescription("RISC-V Assembly Project");
		}

		@Override
		public Image getIcon(int type) {
			return ImageUtilities.loadImage(RiscvProject.ICON);
		}

		@Override
		public Image getOpenedIcon(int type) {
			return getIcon(type);
		}

		@Override
		public Action[] getActions(boolean context) {
			return new Action[]{
				CommonProjectActions.newFileAction(),
				null,
				ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, "Build", null),
				ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, "Clean and Build", null),
				ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, "Clean", null),
				ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, "Run", null),
				null,
				CommonProjectActions.setAsMainProjectAction(),
				CommonProjectActions.copyProjectAction(),
				CommonProjectActions.deleteProjectAction(),
				null,
				CommonProjectActions.closeProjectAction()
			};
		}

		@Override
		public boolean canRename() {
			return false;
		}
	}

	private static final class ProjectChildren extends Children.Keys<FolderKey> {

		private final RiscvProject project;

		ProjectChildren(RiscvProject project) {
			this.project = project;
		}

		@Override
		protected void addNotify() {
			List<FolderKey> keys = new ArrayList<>();
			add(keys, RiscvProject.SRC_DIR, "Source Files");
			add(keys, RiscvProject.INC_DIR, "Include Files");
			add(keys, RiscvProject.LINKER_DIR, "Linker Scripts");
			add(keys, RiscvProject.LIB_DIR, "Libraries");
			FileObject makefile = project.getProjectDirectory().getFileObject("Makefile");
			if (makefile != null && makefile.isData()) {
				keys.add(new FolderKey(makefile, "Makefile"));
			}
			FileObject setting = project.getProjectDirectory().getFileObject("setting.xml");
			if (setting != null && setting.isData()) {
				keys.add(new FolderKey(setting, "setting.xml"));
			}
			setKeys(keys);
		}

		private void add(List<FolderKey> keys, String folderName, String displayName) {
			FileObject folder = project.getProjectDirectory().getFileObject(folderName);
			if (folder != null && folder.isFolder()) {
				keys.add(new FolderKey(folder, displayName));
			}
		}

		@Override
		protected Node[] createNodes(FolderKey key) {
			try {
				DataObject dataObject = DataObject.find(key.folder);
				if (key.folder.isFolder()) {
					return new Node[]{new FolderNode(dataObject.getNodeDelegate(), key.displayName)};
				}
				return new Node[]{new FilterNode(dataObject.getNodeDelegate())};
			} catch (DataObjectNotFoundException ex) {
				return new Node[]{Node.EMPTY};
			}
		}
	}

	private record FolderKey(FileObject folder, String displayName) {
	}

	private static final class FolderNode extends FilterNode {

		FolderNode(Node original, String displayName) {
			super(original, new FilterNode.Children(original), new ProxyLookup(original.getLookup()));
			disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME | DELEGATE_GET_SHORT_DESCRIPTION);
			setDisplayName(displayName);
			setShortDescription(displayName);
		}
	}
}
