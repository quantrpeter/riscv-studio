package hk.quantr.riscvstudio.action;

import hk.quantr.riscvstudio.DummySimulation;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JSlider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.actions.Presenter;

public final class SimulatorActions {

	@ActionID(category = "RISC-V", id = "hk.quantr.riscvstudio.action.Assemble")
	@ActionRegistration(displayName = "Assemble", iconBase = "hk/quantr/riscvstudio/action/assemble.png")
	@ActionReferences({
		@ActionReference(path = "Menu/RISC-V", position = 100),
		@ActionReference(path = "Toolbars/RISC-V", position = 100)
	})
	public static final class Assemble implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			DummySimulation.get().assemble();
		}
	}

	@ActionID(category = "RISC-V", id = "hk.quantr.riscvstudio.action.Reset")
	@ActionRegistration(displayName = "Reset", iconBase = "hk/quantr/riscvstudio/action/reset.png")
	@ActionReferences({
		@ActionReference(path = "Menu/RISC-V", position = 200),
		@ActionReference(path = "Toolbars/RISC-V", position = 200)
	})
	public static final class Reset implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			DummySimulation.get().reset();
		}
	}

	@ActionID(category = "RISC-V", id = "hk.quantr.riscvstudio.action.Run")
	@ActionRegistration(displayName = "Run", iconBase = "hk/quantr/riscvstudio/action/run.png")
	@ActionReferences({
		@ActionReference(path = "Menu/RISC-V", position = 300),
		@ActionReference(path = "Toolbars/RISC-V", position = 300)
	})
	public static final class Run implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			DummySimulation.get().run();
		}
	}

	@ActionID(category = "RISC-V", id = "hk.quantr.riscvstudio.action.Pause")
	@ActionRegistration(displayName = "Pause", iconBase = "hk/quantr/riscvstudio/action/pause.png")
	@ActionReferences({
		@ActionReference(path = "Menu/RISC-V", position = 400),
		@ActionReference(path = "Toolbars/RISC-V", position = 400)
	})
	public static final class Pause implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			DummySimulation.get().pause();
		}
	}

	@ActionID(category = "RISC-V", id = "hk.quantr.riscvstudio.action.StepInto")
	@ActionRegistration(displayName = "Step Into", iconBase = "hk/quantr/riscvstudio/action/stepInto.png")
	@ActionReferences({
		@ActionReference(path = "Menu/RISC-V", position = 500),
		@ActionReference(path = "Toolbars/RISC-V", position = 500)
	})
	public static final class StepInto implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			DummySimulation.get().stepInto();
		}
	}

	@ActionID(category = "RISC-V", id = "hk.quantr.riscvstudio.action.StepOver")
	@ActionRegistration(displayName = "Step Over", iconBase = "hk/quantr/riscvstudio/action/stepOver.png")
	@ActionReferences({
		@ActionReference(path = "Menu/RISC-V", position = 600),
		@ActionReference(path = "Toolbars/RISC-V", position = 600)
	})
	public static final class StepOver implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			DummySimulation.get().stepOver();
		}
	}

	@ActionID(category = "RISC-V", id = "hk.quantr.riscvstudio.action.StepOut")
	@ActionRegistration(displayName = "Step Out", iconBase = "hk/quantr/riscvstudio/action/stepOut.png")
	@ActionReferences({
		@ActionReference(path = "Menu/RISC-V", position = 700),
		@ActionReference(path = "Toolbars/RISC-V", position = 700)
	})
	public static final class StepOut implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			DummySimulation.get().stepOut();
		}
	}

	@ActionID(category = "RISC-V", id = "hk.quantr.riscvstudio.action.RunSpeed")
	@ActionRegistration(displayName = "Run Speed", lazy = false)
	@ActionReference(path = "Toolbars/RISC-V", position = 800)
	public static final class RunSpeed extends AbstractAction implements Presenter.Toolbar {

		private final JSlider slider = new JSlider(0, 100, DummySimulation.get().getSpeed());

		public RunSpeed() {
			slider.setMaximumSize(new Dimension(140, 24));
			slider.setToolTipText("Run Speed");
			slider.addChangeListener(e -> {
				if (!slider.getValueIsAdjusting()) {
					DummySimulation.get().setSpeed(slider.getValue());
				}
			});
		}

		@Override
		public void actionPerformed(ActionEvent e) {
		}

		@Override
		public Component getToolbarPresenter() {
			javax.swing.JPanel panel = new javax.swing.JPanel();
			panel.add(new JLabel("Speed"));
			panel.add(slider);
			return panel;
		}
	}
}
