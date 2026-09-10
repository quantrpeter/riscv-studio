package hk.quantr.riscvstudio.project;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

@TemplateRegistration(
		folder = "Project/RISC-V",
		displayName = "#RiscvAssemblyProject_displayName",
		description = "RiscvAssemblyProject.html",
		iconBase = "hk/quantr/riscvstudio/project/riscv.png",
		position = 100,
		requireProject = false
)
@Messages("RiscvAssemblyProject_displayName=Assembly Project")
public final class RiscvProjectWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

	private int index;
	private WizardDescriptor wizard;
	private WizardDescriptor.Panel<WizardDescriptor>[] panels;

	@Override
	public Set<?> instantiate() throws IOException {
		File dirF = FileUtil.normalizeFile((File) wizard.getProperty("projdir"));
		if (!dirF.mkdirs() && !dirF.isDirectory()) {
			throw new IOException("Could not create project folder: " + dirF);
		}

		FileObject dir = FileUtil.toFileObject(dirF);
		String name = (String) wizard.getProperty("name");
		createProject(dir, name);

		File parent = dirF.getParentFile();
		if (parent != null && parent.exists()) {
			ProjectChooser.setProjectsFolder(parent);
		}

		FileObject source = dir.getFileObject(RiscvProject.SRC_DIR + "/riscv1.asm");
		if (source != null) {
			return Set.of(dir, source);
		}
		return Collections.singleton(dir);
	}

	static void createProject(FileObject dir, String name) throws IOException {
		write(dir, RiscvProjectFactory.PROJECT_FILE, projectMarker(name));
		FileUtil.createFolder(dir, RiscvProject.SRC_DIR);
		FileUtil.createFolder(dir, RiscvProject.INC_DIR);
		FileUtil.createFolder(dir, RiscvProject.LINKER_DIR);
		FileUtil.createFolder(dir, RiscvProject.LIB_DIR);
		write(dir, RiscvProject.SRC_DIR + "/riscv1.asm", sampleAssembly(name));
		write(dir, "Makefile", makefileTemplate());
	}

	private static void write(FileObject dir, String relativePath, String content) throws IOException {
		FileObject file = FileUtil.createData(dir, relativePath);
		try (Writer writer = new OutputStreamWriter(file.getOutputStream(), StandardCharsets.UTF_8)) {
			writer.write(content);
		}
	}

	private static String projectMarker(String name) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<project type="assembly">
				    <name>%s</name>
				</project>
				""".formatted(name);
	}

	private static String sampleAssembly(String name) {
		return """
				# %s
				# RISC-V assembly project

				        .section .text
				        .globl  _start

				_start:
				        j       main

				main:
				        la      a0, msg_hello
				        li      a7, 4
				        ecall
				        li      a0, 0
				        li      a7, 10
				        ecall

				loop:
				        j       loop

				        .section .data
				msg_hello:
				        .asciz  "Hello, RISC-V!"
				""".formatted(name);
	}

	private static String makefileTemplate() throws IOException {
		try (InputStream in = RiscvProjectWizardIterator.class.getResourceAsStream(
				"/hk/quantr/riscvstudio/templates/Makefile")) {
			if (in == null) {
				throw new IOException("Makefile template not found");
			}
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initialize(WizardDescriptor wizard) {
		this.wizard = wizard;
		index = 0;
		panels = new WizardDescriptor.Panel[]{new RiscvProjectWizardPanel()};
		String[] steps = new String[]{"Name and Location"};
		for (int i = 0; i < panels.length; i++) {
			Component component = panels[i].getComponent();
			if (component instanceof JComponent jc) {
				jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
				jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
				jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
				jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
				jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
			}
		}
	}

	@Override
	public void uninitialize(WizardDescriptor wizard) {
		wizard.putProperty("projdir", null);
		wizard.putProperty("name", null);
		this.wizard = null;
		panels = null;
	}

	@Override
	public WizardDescriptor.Panel<WizardDescriptor> current() {
		return panels[index];
	}

	@Override
	public String name() {
		return MessageFormat.format("{0} of {1}", index + 1, panels.length);
	}

	@Override
	public boolean hasNext() {
		return index < panels.length - 1;
	}

	@Override
	public boolean hasPrevious() {
		return index > 0;
	}

	@Override
	public void nextPanel() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		index++;
	}

	@Override
	public void previousPanel() {
		if (!hasPrevious()) {
			throw new NoSuchElementException();
		}
		index--;
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
	}
}
