package se.redfield.arxnode.nodes;

import java.awt.Font;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.FlowVariable.Type;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.AttributeTypeOptions;
import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.ColumnsConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.ui.AnonymizationConfigPanel;
import se.redfield.arxnode.ui.PrivacyModelsPane;
import se.redfield.arxnode.ui.SubsetConfigPanel;
import se.redfield.arxnode.ui.TransformationConfigPanel;

public class AnonymizerNodeDialog extends DefaultNodeSettingsPane {

	private static final NodeLogger logger = NodeLogger.getLogger(AnonymizerNodeDialog.class);
	private static final String PRIVACY_MODELS_TAB_TITLE = "Privacy Models";

	private Config config;
	private JPanel columnsPanel;
	private PrivacyModelsPane privacyPanel;
	private AnonymizationConfigPanel anonConfigPanel;

	protected AnonymizerNodeDialog() {
		super();
		logger.info("Dialog.constructor");

		config = new Config();

		columnsPanel = new JPanel();
		privacyPanel = new PrivacyModelsPane(config);
		anonConfigPanel = new AnonymizationConfigPanel(config.getAnonymizationConfig(), this);

		addTab("Columns", columnsPanel);
		addTab(PRIVACY_MODELS_TAB_TITLE, privacyPanel.getComponent(), false);
		addTab("Anonymization Config", anonConfigPanel.getComponent());
		addTab("Research sample", new SubsetConfigPanel(config.getSubsetConfig()));
		selectTab("Columns");
		removeTab("Options");

	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
		logger.info("Dialog.loadSettings");
		DataTableSpec inTableSpec = (DataTableSpec) specs[AnonymizerNodeModel.PORT_DATA_TABLE];
		if ((inTableSpec == null) || (inTableSpec.getNumColumns() < 1)) {
			throw new NotConfigurableException("Cannot be configured without" + " input table");
		}

		try {
			config.load(settings);
		} catch (InvalidSettingsException e) {
			logger.debug(e.getMessage(), e);
		}
		config.configure(inTableSpec, (ArxPortObjectSpec) specs[AnonymizerNodeModel.PORT_ARX]);
		initColumnsPanel(settings, inTableSpec);
		anonConfigPanel.load(settings, specs);
	}

	private void initColumnsPanel(NodeSettingsRO settings, DataTableSpec spec) {
		columnsPanel.removeAll();

		String rowSpec = "15:n, p:n";
		for (int i = 0; i < config.getColumns().size() - 1; i++) {
			rowSpec += ",5:n, p:n";
		}
		rowSpec += ",15:n";
		CellConstraints cc = new CellConstraints();
		columnsPanel.setLayout(new FormLayout("15:n, f:p:g, 15:n", rowSpec));

		ColumnConfig[] columns = new ColumnConfig[config.getColumns().size()];
		config.getColumns().forEach(c -> columns[c.getIndex()] = c);
		for (int i = 0; i < columns.length; i++) {
			columnsPanel.add(createColumnRow(columns[i]), cc.rc(i * 2 + 2, 2));
		}
	}

	private JPanel createColumnRow(ColumnConfig c) {
		SettingsModelString attrTypeModel = c.getAttrTypeModel();
		createFlowVariableModel(new String[] { ColumnsConfig.CONFIG_KEY, c.getName(), ColumnConfig.CONFIG_ATTR_TYPE },
				FlowVariable.Type.STRING, attrTypeModel);

		SettingsModelString fileModel = c.getHierarchyFileModel();
		DialogComponentFileChooser fileChooser = new DialogComponentFileChooser(fileModel, "ArxNode",
				JFileChooser.OPEN_DIALOG, false,
				createFlowVariableModel(
						new String[] { ColumnsConfig.CONFIG_KEY, c.getName(), ColumnConfig.CONFIG_ATTR_TYPE },
						Type.STRING),
				"ahs");
		if (c.isHierarchyOverriden()) {
			fileChooser.setBorderTitle("Hierarchy is already provided by config overrides");
		}

		TransformationConfigPanel transformationPanel = new TransformationConfigPanel(this, c);

		attrTypeModel.addChangeListener(
				e -> onAttrTypeChanged(fileModel, attrTypeModel, fileChooser, transformationPanel, false));
		onAttrTypeChanged(fileModel, attrTypeModel, fileChooser, transformationPanel, true);

		JLabel columnLabel = new JLabel(createHtmlLabel(c));
		Font font = UIManager.getFont("Label.font");
		columnLabel.setFont(new Font(font.getName(), font.getStyle(), font.getSize() + 2));

		CellConstraints cc = new CellConstraints();
		JPanel row = new JPanel(new FormLayout("l:p:n, 5:n, r:p:g", "p:n, 5:n, p:n, 5:n, p:n"));
		row.add(columnLabel, cc.rc(1, 1));
		row.add(new DialogComponentStringSelection(attrTypeModel, "Type:", AttributeTypeOptions.stringValues())
				.getComponentPanel(), cc.rc(1, 3));
		row.add(fileChooser.getComponentPanel(), cc.rcw(3, 1, 3));
		row.add(transformationPanel, cc.rcw(5, 1, 3));
		return row;
	}

	private String createHtmlLabel(ColumnConfig c) {
		String html = "<html><b>%1$s</b>%2$s</html>";
		String asterisk = c.isHierarchyOverriden() ? "<font color=red>*</font>" : "";
		return String.format(html, c.getName(), asterisk);
	}

	private void onAttrTypeChanged(SettingsModelString fileModel, SettingsModelString attrTypeModel,
			DialogComponentFileChooser fileChooser, TransformationConfigPanel transformationConfig, boolean init) {
		AttributeTypeOptions opt = AttributeTypeOptions.fromName(attrTypeModel.getStringValue());
		boolean qiAttr = opt == AttributeTypeOptions.QUASI_IDENTIFYING_ATTRIBUTE;
		fileChooser.getComponentPanel().setVisible(qiAttr);
		transformationConfig.setVisible(qiAttr);

		if (!init && opt == AttributeTypeOptions.SENSITIVE_ATTRIBUTE) {
			setSelected(PRIVACY_MODELS_TAB_TITLE);
		}
	}

	@Override
	public void saveAdditionalSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		logger.info("Dialog.saveSettings");
		super.saveAdditionalSettingsTo(settings);
		config.save(settings);
	}

	public FlowVariableModel createFlowVariableModel(String[] keys, FlowVariable.Type type, SettingsModel model) {
		FlowVariableModel fvm = createFlowVariableModel(keys, type);
		if (model != null) {
			fvm.addChangeListener(e -> model.setEnabled(!fvm.isVariableReplacementEnabled()));
		}
		return fvm;
	}

}
