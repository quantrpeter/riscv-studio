package hk.quantr.riscvstudio.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ProjectFactory.class)
public class RiscvProjectFactory implements ProjectFactory2 {

	public static final String PROJECT_FILE = "riscv.project";

	@Override
	public boolean isProject(FileObject projectDirectory) {
		return projectDirectory.getFileObject(PROJECT_FILE) != null;
	}

	@Override
	public ProjectManager.Result isProject2(FileObject projectDirectory) {
		if (!isProject(projectDirectory)) {
			return null;
		}
		return new ProjectManager.Result(ImageUtilities.loadImageIcon(RiscvProject.ICON, true));
	}

	@Override
	public Project loadProject(FileObject dir, ProjectState state) throws IOException {
		return isProject(dir) ? new RiscvProject(dir, state) : null;
	}

	@Override
	public void saveProject(Project project) throws IOException {
	}
}
