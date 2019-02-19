package se.redfield.arxnode;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.AttributeTypeOptions;
import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.ui.AnonymizationConfigPanel;
import se.redfield.arxnode.ui.PrivacyModelsPane;
import se.redfield.arxnode.ui.SubsetConfigPanel;
import se.redfield.arxnode.ui.TransformationConfigPanel;

public class ArxNodeNodeDialog extends DefaultNodeSettingsPane {

	private static final NodeLogger logger = NodeLogger.getLogger(ArxNodeNodeDialog.class);
	private static final String PRIVACY_MODELS_TAB_TITLE = "Privacy Models";

	private Config config;
	private JPanel columnsPanel;
	private PrivacyModelsPane privacyPanel;
	private AnonymizationConfigPanel anonConfigPanel;

	protected ArxNodeNodeDialog() {
		super();
		logger.info("Dialog.constructor");
		// addDialogComponent(
		// new DialogComponentNumber(new
		// SettingsModelIntegerBounded(Config.CONFIG_KANONYMITY_FACTOR_KEY,
		// Config.DEFAULT_KANONYMITY_FACTOR, 1, Integer.MAX_VALUE), "K-Anonymity
		// factor:", 1, 5));

		config = new Config();

		columnsPanel = new JPanel();
		privacyPanel = new PrivacyModelsPane(config);
		anonConfigPanel = new AnonymizationConfigPanel(config.getAnonymizationConfig());
		addTab("Columns", columnsPanel);
		addTab(PRIVACY_MODELS_TAB_TITLE, privacyPanel.getComponent(), false);
		addTab("Anonymization Config", anonConfigPanel.getComponent());
		addTab("Research sample", new SubsetConfigPanel(config.getSubsetConfig()));
		selectTab("Columns");
		removeTab("Options");

	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
			throws NotConfigurableException {
		logger.info("Dialog.loadSettings");
		if ((specs[0] == null) || (specs[0].getNumColumns() < 1)) {
			throw new NotConfigurableException("Cannot be configured without" + " input table");
		}

		try {
			config.load(settings);
		} catch (InvalidSettingsException e) {
			logger.debug(e.getMessage(), e);
		}
		config.initColumns(specs[0]);
		initColumnsPanel(settings, specs[0]);
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
		SettingsModelString fileModel = c.getHierarchyFileModel();
		SettingsModelString attrTypeModel = c.getAttrTypeModel();
		DialogComponentFileChooser fileChooser = new DialogComponentFileChooser(fileModel, "ArxNode", "ahs");
		TransformationConfigPanel transformationPanel = new TransformationConfigPanel(this, c);

		attrTypeModel.addChangeListener(
				e -> onAttrTypeChanged(fileModel, attrTypeModel, fileChooser, transformationPanel, false));
		onAttrTypeChanged(fileModel, attrTypeModel, fileChooser, transformationPanel, true);

		JLabel columnLabel = new JLabel(c.getName());
		Font font = UIManager.getFont("Label.font");
		columnLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 2));

		CellConstraints cc = new CellConstraints();
		JPanel row = new JPanel(new FormLayout("l:p:n, 5:n, r:p:g", "p:n, 5:n, p:n, 5:n, p:n"));
		row.add(columnLabel, cc.rc(1, 1));
		row.add(new DialogComponentStringSelection(attrTypeModel, "Type:", AttributeTypeOptions.stringValues())
				.getComponentPanel(), cc.rc(1, 3));
		row.add(fileChooser.getComponentPanel(), cc.rcw(3, 1, 3));
		row.add(transformationPanel, cc.rcw(5, 1, 3));
		return row;
	}

	private void onAttrTypeChanged(SettingsModelString fileModel, SettingsModelString attrTypeModel,
			DialogComponentFileChooser fileChooser, TransformationConfigPanel transformationConfig, boolean init) {
		AttributeTypeOptions opt = AttributeTypeOptions.fromName(attrTypeModel.getStringValue());
		fileModel.setEnabled(opt == AttributeTypeOptions.QUASI_IDENTIFYING_ATTRIBUTE);
		if (!fileModel.isEnabled()) {
			fileModel.setStringValue("");
		}
		fileChooser.getComponentPanel().setVisible(fileModel.isEnabled());
		transformationConfig.setVisible(fileModel.isEnabled());

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

}
