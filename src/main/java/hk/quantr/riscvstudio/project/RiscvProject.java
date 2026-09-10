package hk.quantr.riscvstudio.project;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class RiscvProject implements Project {

	public static final String ICON = "hk/quantr/riscvstudio/project/riscv.png";
	public static final String TYPE = "hk.quantr.riscvstudio.assembly";

	public static final String SRC_DIR = "src";
	public static final String INC_DIR = "inc";
	public static final String LINKER_DIR = "linker";
	public static final String LIB_DIR = "lib";

	private final FileObject projectDir;
	private final ProjectState state;
	private Lookup lookup;

	RiscvProject(FileObject dir, ProjectState state) {
		this.projectDir = dir;
		this.state = state;
	}

	@Override
	public FileObject getProjectDirectory() {
		return projectDir;
	}

	@Override
	public Lookup getLookup() {
		if (lookup == null) {
			lookup = Lookups.fixed(
					this,
					state,
					new Info(),
					new RiscvLogicalView(this),
					new RiscvActionProvider(this),
					new DeleteOperation(),
					new Templates()
			);
		}
		return lookup;
	}

	public FileObject getSourceFolder() {
		return projectDir.getFileObject(SRC_DIR);
	}

	private final class Info implements ProjectInformation {

		@Override
		public String getName() {
			return projectDir.getName();
		}

		@Override
		public String getDisplayName() {
			return projectDir.getName();
		}

		@Override
		public Icon getIcon() {
			return ImageUtilities.loadImageIcon(ICON, true);
		}

		@Override
		public Project getProject() {
			return RiscvProject.this;
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
		}
	}

	private final class DeleteOperation implements DeleteOperationImplementation {

		@Override
		public void notifyDeleting() throws IOException {
		}

		@Override
		public void notifyDeleted() throws IOException {
		}

		@Override
		public List<FileObject> getMetadataFiles() {
			List<FileObject> files = new ArrayList<>();
			FileObject marker = projectDir.getFileObject(RiscvProjectFactory.PROJECT_FILE);
			if (marker != null) {
				files.add(marker);
			}
			return files;
		}

		@Override
		public List<FileObject> getDataFiles() {
			List<FileObject> files = new ArrayList<>();
			collect(projectDir, files);
			return files;
		}

		private void collect(FileObject folder, List<FileObject> files) {
			for (FileObject child : folder.getChildren()) {
				if (RiscvProjectFactory.PROJECT_FILE.equals(child.getNameExt())) {
					continue;
				}
				files.add(child);
				if (child.isFolder()) {
					collect(child, files);
				}
			}
		}
	}

	private static final class Templates implements PrivilegedTemplates, RecommendedTemplates {

		@Override
		public String[] getPrivilegedTemplates() {
			return new String[]{"Templates/RISC-V/AssemblyFile.asm"};
		}

		@Override
		public String[] getRecommendedTypes() {
			return new String[]{"RISC-V"};
		}
	}
}
