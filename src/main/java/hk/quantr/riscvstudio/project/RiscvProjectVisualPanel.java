package hk.quantr.riscvstudio.project;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;

public class RiscvProjectVisualPanel extends JPanel {

	private final RiscvProjectWizardPanel panel;
	private final JTextField nameField = new JTextField();
	private final JTextField locationField = new JTextField();
	private final JTextField folderField = new JTextField();
	private boolean ignoreUpdate;

	RiscvProjectVisualPanel(RiscvProjectWizardPanel panel) {
		this.panel = panel;
		setLayout(new GridBagLayout());

		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridx = 0;
		labelConstraints.anchor = GridBagConstraints.LINE_START;
		labelConstraints.insets = new Insets(4, 0, 4, 8);

		GridBagConstraints fieldConstraints = new GridBagConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.weightx = 1;
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldConstraints.insets = new Insets(4, 0, 4, 0);

		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.gridx = 2;
		buttonConstraints.insets = new Insets(4, 8, 4, 0);

		labelConstraints.gridy = 0;
		fieldConstraints.gridy = 0;
		add(new JLabel("Project Name:"), labelConstraints);
		add(nameField, fieldConstraints);

		labelConstraints.gridy = 1;
		fieldConstraints.gridy = 1;
		buttonConstraints.gridy = 1;
		JButton browse = new JButton("Browse...");
		browse.addActionListener(e -> browseLocation());
		add(new JLabel("Project Location:"), labelConstraints);
		add(locationField, fieldConstraints);
		add(browse, buttonConstraints);

		labelConstraints.gridy = 2;
		fieldConstraints.gridy = 2;
		folderField.setEditable(false);
		add(new JLabel("Project Folder:"), labelConstraints);
		add(folderField, fieldConstraints);

		GridBagConstraints filler = new GridBagConstraints();
		filler.gridx = 0;
		filler.gridy = 3;
		filler.gridwidth = 3;
		filler.weighty = 1;
		filler.fill = GridBagConstraints.BOTH;
		add(new JPanel(), filler);

		DocumentListener listener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				changed();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changed();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				changed();
			}
		};
		nameField.getDocument().addDocumentListener(listener);
		locationField.getDocument().addDocumentListener(listener);
	}

	void read(WizardDescriptor wizard) {
		ignoreUpdate = true;
		try {
			String name = (String) wizard.getProperty("name");
			File location = (File) wizard.getProperty("projdir");
			if (name == null) {
				name = uniqueName(RiscvProjectWizardPanel.defaultProjectsFolder());
			}
			if (location == null) {
				location = new File(RiscvProjectWizardPanel.defaultProjectsFolder(), name);
			}
			nameField.setText(name);
			File parent = location.getParentFile();
			locationField.setText(parent != null ? parent.getAbsolutePath() : location.getAbsolutePath());
			updateFolder();
		} finally {
			ignoreUpdate = false;
		}
	}

	void store(WizardDescriptor wizard) {
		wizard.putProperty("name", nameField.getText().trim());
		wizard.putProperty("projdir", new File(folderField.getText()));
	}

	String validateProject() {
		String name = nameField.getText().trim();
		if (name.isEmpty()) {
			return "Project Name is required.";
		}
		if (!FileUtil.isValidFileName(name) || name.contains("/") || name.contains("\\")) {
			return "Project Name is not a valid folder name.";
		}
		String location = locationField.getText().trim();
		if (location.isEmpty()) {
			return "Project Location is required.";
		}
		File locationFile = new File(location);
		if (locationFile.exists() && !locationFile.isDirectory()) {
			return "Project Location is not a folder.";
		}
		File projectFolder = new File(folderField.getText());
		if (projectFolder.exists()) {
			File[] children = projectFolder.listFiles();
			if (children != null && children.length > 0) {
				return "Project Folder already exists and is not empty.";
			}
		}
		return null;
	}

	private void browseLocation() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select Project Location");
		File current = new File(locationField.getText().trim());
		if (current.isDirectory()) {
			chooser.setSelectedFile(current);
		}
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			locationField.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void changed() {
		if (ignoreUpdate) {
			return;
		}
		updateFolder();
		panel.fireChange();
	}

	private void updateFolder() {
		File location = new File(locationField.getText().trim());
		folderField.setText(new File(location, nameField.getText().trim()).getAbsolutePath());
	}

	private static String uniqueName(File parent) {
		String base = "RiscvAssembly";
		File candidate = new File(parent, base);
		int index = 1;
		while (candidate.exists()) {
			candidate = new File(parent, base + "_" + index++);
		}
		return candidate.getName();
	}
}
