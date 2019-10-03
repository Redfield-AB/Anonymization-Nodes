package org.knime.core.node;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.config.ConfigEditTreeModel;
import org.knime.core.node.util.FlowVariableListCellRenderer;
import org.knime.core.node.util.SharedIcons;
import org.knime.core.node.workflow.FlowVariable;

public class FlowVariablesButton extends JButton implements ActionListener, ChangeListener {
	private static final long serialVersionUID = 1L;

	private NodeDialogPane dlg;
	private List<FlowVariableModel> models;

	public FlowVariablesButton(NodeDialogPane dlg) {
		this.dlg = dlg;
		this.models = new ArrayList<>();
		setBorder(new LineBorder(Color.gray));
		addActionListener(this);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		updateIcon();
	}

	private void updateIcon() {
		boolean enabled = this.models.stream().anyMatch(m -> m.isVariableReplacementEnabled());
		Icon icon = enabled ? SharedIcons.FLOWVAR_ACTIVE.get() : SharedIcons.FLOWVAR_INACTIVE.get();
		setIcon(icon);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Frame parent = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, this);
		FlowVariablesDlg dlg = new FlowVariablesDlg(parent);
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);
	}

	public void setModels(Collection<FlowVariableModel> models) {
		this.models.clear();
		this.models.addAll(models);
		for (FlowVariableModel m : models) {
			m.addChangeListener(this);
		}
		updateIcon();
	}

	private Collection<FlowVariable> getMatchingVariables(FlowVariableModel m) {
		return dlg.getAvailableFlowVariables().values().stream()
				.filter(fv -> ConfigEditTreeModel.doesTypeAccept(m.getType(), fv.getType()))
				.collect(Collectors.toList());
	}

	private class FlowVariablesDlg extends JDialog {
		private static final long serialVersionUID = 1L;

		private List<FlowVarControlsBinding> bindings;

		public FlowVariablesDlg(Frame parent) {
			super(parent, "Flow Variables", true);
			bindings = new ArrayList<>();

			setIconImage(((ImageIcon) SharedIcons.FLOWVAR_DIALOG_ACTIVE.get()).getImage());

			initUI();
			pack();
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		}

		private void initUI() {
			Container pane = getContentPane();
			pane.add(createMainPanel(), BorderLayout.CENTER);
			pane.add(createButtonsPanel(), BorderLayout.PAGE_END);
		}

		private JPanel createMainPanel() {
			JPanel title = new JPanel();
			title.setLayout(new BoxLayout(title, BoxLayout.PAGE_AXIS));
			JPanel input = new JPanel();
			input.setLayout(new BoxLayout(input, BoxLayout.PAGE_AXIS));
			JPanel output = new JPanel();
			output.setLayout(new BoxLayout(output, BoxLayout.PAGE_AXIS));

			createHeader(title, input, output);
			models.forEach(m -> createRow(m, title, input, output));
			title.add(Box.createVerticalGlue());
			input.add(Box.createVerticalGlue());
			output.add(Box.createVerticalGlue());

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.add(title);
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
			panel.add(input);
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
			panel.add(output);
			panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
			return panel;
		}

		private void createHeader(JPanel title, JPanel input, JPanel output) {
			Font labelFont = UIManager.getFont("Label.font");
			Font font = new Font(labelFont.getName(), Font.BOLD, labelFont.getSize());

			JLabel titleHeader = new JLabel();
			JLabel inputHeader = new JLabel("Input Var");
			JLabel outputHeader = new JLabel("Output Var");

			titleHeader.setFont(font);
			inputHeader.setFont(font);
			outputHeader.setFont(font);

			titleHeader.setAlignmentX(CENTER_ALIGNMENT);
			inputHeader.setAlignmentX(CENTER_ALIGNMENT);
			outputHeader.setAlignmentX(CENTER_ALIGNMENT);

			adjustRowHeight(titleHeader, inputHeader, outputHeader);

			title.add(titleHeader);
			title.add(Box.createRigidArea(new Dimension(0, 5)));
			input.add(inputHeader);
			input.add(Box.createRigidArea(new Dimension(0, 5)));
			output.add(outputHeader);
			output.add(Box.createRigidArea(new Dimension(0, 5)));
		}

		private void adjustRowHeight(JComponent... row) {
			int height = Arrays.stream(row).map(c -> c.getPreferredSize().height).max(Integer::compare).orElse(0);
			for (JComponent c : row) {
				c.setPreferredSize(new Dimension(c.getPreferredSize().width, height));
				c.setMaximumSize(new Dimension(c.getMaximumSize().width, height));
			}
		}

		private void createRow(FlowVariableModel m, JPanel title, JPanel input, JPanel output) {
			String name = m.getKeys()[m.getKeys().length - 1];
			JLabel lName = new JLabel(name);
			title.add(lName);
			title.add(Box.createRigidArea(new Dimension(0, 5)));

			JComboBox<Object> cbInput = createInputCombo(m);
			input.add(cbInput);
			input.add(Box.createRigidArea(new Dimension(0, 5)));

			JTextField tfOutput = new JTextField(15);
			output.add(tfOutput);
			output.add(Box.createRigidArea(new Dimension(0, 5)));

			adjustRowHeight(lName, cbInput, tfOutput);
			bindings.add(new FlowVarControlsBinding(m, cbInput, tfOutput));
		}

		private JPanel createButtonsPanel() {
			JButton bOk = new JButton("Ok");
			bOk.addActionListener(e -> {
				bindings.forEach(b -> b.readFromControls());
				setVisible(false);
			});

			JButton bCancel = new JButton("Cancel");
			bCancel.addActionListener(e -> setVisible(false));

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.add(Box.createHorizontalGlue());
			panel.add(bOk);
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
			panel.add(bCancel);
			panel.add(Box.createHorizontalGlue());
			return panel;
		}

		private JComboBox<Object> createInputCombo(FlowVariableModel m) {
			DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();

			Collection<FlowVariable> availableFw = getMatchingVariables(m);
			if (availableFw.size() > 0) {
				model.addElement("<not set>");
				for (FlowVariable fw : availableFw) {
					model.addElement(fw);
				}
			} else {
				model.addElement("<no matching vars>");
			}

			JComboBox<Object> cb = new JComboBox<>(model);
			cb.setRenderer(new FlowVariableListCellRenderer());
			return cb;
		}

	}

	private class FlowVarControlsBinding {
		private FlowVariableModel model;
		private JComboBox<?> input;
		private JTextField output;

		public FlowVarControlsBinding(FlowVariableModel model, JComboBox<?> input, JTextField output) {
			this.model = model;
			this.input = input;
			this.output = output;
			populateControls();
		}

		private void populateControls() {
			if (!StringUtils.isEmpty(model.getInputVariableName())) {
				getMatchingVariables(model).stream().filter(fv -> fv.getName().equals(model.getInputVariableName()))
						.findFirst().ifPresent(fv -> input.setSelectedItem(fv));
			}
			if (model.getOutputVariableName() != null) {
				output.setText(model.getOutputVariableName());
			}
		}

		public void readFromControls() {
			Object selected = input.getSelectedItem();
			if (selected != null && selected instanceof FlowVariable) {
				model.setInputVariableName(((FlowVariable) selected).getName());
			} else {
				model.setInputVariableName(null);
			}
			if (!StringUtils.isEmpty(output.getText())) {
				model.setOutputVariableName(output.getText());
			} else {
				model.setOutputVariableName(null);
			}
		}
	}

}
