package se.redfield.arxnode.nodes;

import org.knime.core.data.DataValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.port.PortObjectSpec;

import se.redfield.arxnode.config.HierarchyExpandConfig;

public class HierarchyExpandNodeDialog extends DefaultNodeSettingsPane {

	private HierarchyExpandConfig config;
	private DialogComponentColumnNameSelection columnInput;
	private DialogComponentFileChooser fileInput;

	@SuppressWarnings("unchecked")
	public HierarchyExpandNodeDialog() {
		super();
		config = new HierarchyExpandConfig();
		columnInput = new DialogComponentColumnNameSelection(config.getColumnSetting(), "Column", 0, DataValue.class);
		fileInput = new DialogComponentFileChooser(config.getFile(), "arx", "ahs");
		addDialogComponent(columnInput);
		addDialogComponent(fileInput);
	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
		super.loadAdditionalSettingsFrom(settings, specs);
		columnInput.loadSettingsFrom(settings, specs);
	}
}
