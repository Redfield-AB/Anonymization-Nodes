package se.redfield.arxnode.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.FlowVariablesButton;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.workflow.FlowVariable.Type;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.ColumnsConfig;
import se.redfield.arxnode.config.TransformationConfig;
import se.redfield.arxnode.config.TransformationConfig.MicroaggregationFunction;
import se.redfield.arxnode.config.TransformationConfig.Mode;

public class TransformationConfigPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final NodeLogger logger = NodeLogger.getLogger(TransformationConfigPanel.class);

	private NodeDialogPane dlg;
	private ColumnConfig columnConfig;
	private TransformationConfig transformationConfig;
	private MicroaggregationFunction[] microaggregationOptions;

	private SettingsModelDoubleBounded weightSetting;
	private JPanel generalizationPanel;
	private JPanel microaggregationPanel;
	private FlowVariablesButton fwButton;
	private JCheckBox cbMin;
	private JCheckBox cbMax;

	private FlowVariableModel fwWeight;
	private FlowVariableModel fwMinLevel;
	private FlowVariableModel fwMaxLevel;
	private FlowVariableModel fwMAFunc;
	private FlowVariableModel fwIgnoreMissing;

	public TransformationConfigPanel(NodeDialogPane dlg, ColumnConfig config) {
		this.dlg = dlg;
		this.columnConfig = config;
		this.transformationConfig = config.getTransformationConfig();
		this.microaggregationOptions = MicroaggregationFunction.values(config.getDataType());
		this.weightSetting = config.getWeightModel();
		initFw();
		initUI();

	}

	private void initFw() {
		fwWeight = dlg.createFlowVariableModel(
				new String[] { ColumnsConfig.CONFIG_KEY, columnConfig.getName(), ColumnConfig.CONFIG_WEIGHT },
				Type.DOUBLE);
		fwMinLevel = dlg.createFlowVariableModel(new String[] { ColumnsConfig.CONFIG_KEY, columnConfig.getName(),
				TransformationConfig.CONFIG_KEY, TransformationConfig.CONFIG_MIN_LEVEL }, Type.INTEGER);
		fwMaxLevel = dlg.createFlowVariableModel(new String[] { ColumnsConfig.CONFIG_KEY, columnConfig.getName(),
				TransformationConfig.CONFIG_KEY, TransformationConfig.CONFIG_MAX_LEVEL }, Type.INTEGER);
		fwMAFunc = dlg.createFlowVariableModel(new String[] { ColumnsConfig.CONFIG_KEY, columnConfig.getName(),
				TransformationConfig.CONFIG_KEY, TransformationConfig.CONFIG_MA_FUNC }, Type.STRING);
		fwIgnoreMissing = dlg
				.createFlowVariableModel(
						new String[] { ColumnsConfig.CONFIG_KEY, columnConfig.getName(),
								TransformationConfig.CONFIG_KEY, TransformationConfig.CONFIG_IGNORE_MISSING },
						Type.STRING);
	}

	private void initUI() {
		generalizationPanel = createGeneralizationPanel();
		microaggregationPanel = createMicroaggregationPanel();
		fwButton = new FlowVariablesButton(dlg);

		JComboBox<Mode> cbMode = new JComboBox<>(Mode.values());
		cbMode.addActionListener(e -> {
			onModeSelected((Mode) cbMode.getSelectedItem());
		});
		cbMode.setSelectedItem(transformationConfig.getMode());

		setLayout(new FormLayout("p:n, 5:n, p:n, 5:n, p:n, f:p:g, p:n", "p:n, 5:n, f:p:n, 5:n, f:p:n"));
		CellConstraints cc = new CellConstraints();
		add(new JLabel("Mode"), cc.rc(1, 1));
		add(cbMode, cc.rc(1, 3));
		add(new DialogComponentNumber(weightSetting, "Weight", 0.05).getComponentPanel(), cc.rc(1, 5));
		add(fwButton, cc.rc(1, 7));
		add(generalizationPanel, cc.rcw(3, 1, 7));
		add(microaggregationPanel, cc.rcw(5, 1, 7));
		setBorder(BorderFactory.createTitledBorder("Transformation"));
	}

	private void onModeSelected(Mode mode) {
		transformationConfig.setMode(mode);

		updateFwButton();

		boolean generalization = mode == Mode.GENERALIZATION;
		generalizationPanel.setVisible(generalization);
		microaggregationPanel.setVisible(!generalization);
	}

	private void updateFwButton() {
		List<FlowVariableModel> models = new ArrayList<>();
		models.add(fwWeight);

		Mode mode = transformationConfig.getMode();

		if (mode == Mode.GENERALIZATION) {
			if (cbMin.isSelected()) {
				models.add(fwMinLevel);
			}
			if (cbMax.isSelected()) {
				models.add(fwMaxLevel);
			}
		} else {
			models.add(fwMAFunc);
			models.add(fwIgnoreMissing);
		}

		fwButton.setModels(models);
	}

	private JPanel createGeneralizationPanel() {
		cbMin = new JCheckBox("Minimum");
		cbMax = new JCheckBox("Maximum");
		JSpinner minInput = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		JSpinner maxInput = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

		cbMin.addActionListener(new MinMaxCheckboxListener(minInput, transformationConfig::setMinGeneralization));
		cbMax.addActionListener(new MinMaxCheckboxListener(maxInput, transformationConfig::setMaxGeneralization));
		minInput.addChangeListener(e -> transformationConfig.setMinGeneralization((Integer) minInput.getValue()));
		maxInput.addChangeListener(e -> transformationConfig.setMaxGeneralization((Integer) maxInput.getValue()));

		setGeneralizationValue(cbMin, minInput, transformationConfig.getMinGeneralization());
		setGeneralizationValue(cbMax, maxInput, transformationConfig.getMaxGeneralization());

		JPanel panel = new JPanel(new FormLayout("p:n, 5:n, p:n, 5:n, p:n, 5:n, p:n", "p:n"));
		CellConstraints cc = new CellConstraints();
		panel.add(cbMin, cc.rc(1, 1));
		panel.add(minInput, cc.rc(1, 3));
		panel.add(cbMax, cc.rc(1, 5));
		panel.add(maxInput, cc.rc(1, 7));

		return panel;
	}

	private void setGeneralizationValue(JCheckBox cb, JSpinner input, Integer value) {
		if (value == null) {
			cb.setSelected(false);
			input.setEnabled(false);
		} else {
			cb.setSelected(true);
			input.setValue(value);
		}
	}

	private JPanel createMicroaggregationPanel() {
		JComboBox<MicroaggregationFunction> cbFunc = new JComboBox<>(microaggregationOptions);
		cbFunc.setSelectedItem(transformationConfig.getMicroaggregationFunc());
		cbFunc.addActionListener(
				e -> transformationConfig.setMicroaggregationFunc((MicroaggregationFunction) cbFunc.getSelectedItem()));

		JCheckBox cbIgnore = new JCheckBox("Ignore missing data");
		cbIgnore.setSelected(transformationConfig.isIgnoreMissingData());
		cbIgnore.addActionListener(e -> transformationConfig.setIgnoreMissingData(cbIgnore.isSelected()));

		CellConstraints cc = new CellConstraints();
		JPanel panel = new JPanel(new FormLayout("p:n, 5:n, p:n, 5:n, p:n", "p:n"));
		panel.add(new JLabel("Function"), cc.rc(1, 1));
		panel.add(cbFunc, cc.rc(1, 3));
		panel.add(cbIgnore, cc.rc(1, 5));
		return panel;
	}

	private class MinMaxCheckboxListener implements ActionListener {

		private JSpinner input;
		private Consumer<Integer> setter;

		public MinMaxCheckboxListener(JSpinner input, Consumer<Integer> setter) {
			this.input = input;
			this.setter = setter;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean selected = ((JCheckBox) e.getSource()).isSelected();
			input.setEnabled(selected);
			setter.accept(selected ? (Integer) input.getValue() : null);
			updateFwButton();
		}

	}
}
