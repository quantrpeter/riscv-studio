package hk.quantr.riscvstudio.project;

import java.awt.Component;
import java.io.File;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.ChangeSupport;

public class RiscvProjectWizardPanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {

	private WizardDescriptor wizard;
	private RiscvProjectVisualPanel component;
	private final ChangeSupport changeSupport = new ChangeSupport(this);
	private boolean valid = true;

	@Override
	public Component getComponent() {
		if (component == null) {
			component = new RiscvProjectVisualPanel(this);
			component.setName("Name and Location");
		}
		return component;
	}

	@Override
	public HelpCtx getHelp() {
		return HelpCtx.DEFAULT_HELP;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		changeSupport.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		changeSupport.removeChangeListener(listener);
	}

	@Override
	public void readSettings(WizardDescriptor wizard) {
		this.wizard = wizard;
		component.read(wizard);
		validateQuietly();
	}

	@Override
	public void storeSettings(WizardDescriptor wizard) {
		component.store(wizard);
	}

	@Override
	public void validate() throws WizardValidationException {
		String message = component.validateProject();
		if (message != null) {
			throw new WizardValidationException(component, message, message);
		}
	}

	void fireChange() {
		validateQuietly();
		changeSupport.fireChange();
	}

	private void validateQuietly() {
		if (component == null || wizard == null) {
			return;
		}
		String message = component.validateProject();
		valid = message == null;
		wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
	}

	static File defaultProjectsFolder() {
		return org.netbeans.spi.project.ui.support.ProjectChooser.getProjectsFolder();
	}
}
