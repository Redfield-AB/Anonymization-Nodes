package se.redfield.arxnode.nodes;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import se.redfield.arxnode.config.HierarchyBinding;
import se.redfield.arxnode.config.HierarchyExpandNodeConfig;
import se.redfield.arxnode.ui.HierarchyBindingPanel;
import se.redfield.arxnode.ui.HierarchyBindingPanel.HierarchyBindingPanelListener;

public class HierarchyExpandNodeDialog extends NodeDialogPane implements HierarchyBindingPanelListener {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyExpandNodeDialog.class);

	private HierarchyExpandNodeConfig config;
	private JPanel columnsPanel;

	private NodeSettingsRO settings;
	private PortObjectSpec[] specs;

	public HierarchyExpandNodeDialog() {
		super();
		config = new HierarchyExpandNodeConfig();

		columnsPanel = new JPanel();
		columnsPanel.setLayout(new BoxLayout(columnsPanel, BoxLayout.Y_AXIS));

		addTab("Hierarchies", columnsPanel);
	}

	@Override
	public void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		this.settings = settings;
		this.specs = specs;
		try {
			config.load(settings);
		} catch (InvalidSettingsException e) {
			logger.error(e.getMessage(), e);
		}

		columnsPanel.removeAll();

		for (HierarchyBinding h : config.getBindings()) {
			columnsPanel.add(createBindingPanel(h));
		}

		if (config.getBindings().size() == 0) {
			onAdd(null);
		}
	}

	private HierarchyBindingPanel createBindingPanel(HierarchyBinding binding) throws NotConfigurableException {
		HierarchyBindingPanel panel = new HierarchyBindingPanel(binding, this);
		panel.loadSettingsFrom(settings, specs);
		return panel;
	}

	public void onAdd(HierarchyBindingPanel panel) {
		try {
			HierarchyBinding b = new HierarchyBinding();
			config.getBindings().add(b);
			columnsPanel.add(createBindingPanel(b));
			columnsPanel.revalidate();
		} catch (NotConfigurableException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void onRemove(HierarchyBindingPanel panel) {
		config.getBindings().remove(panel.getBinding());
		columnsPanel.remove(panel);
		columnsPanel.revalidate();
		if (config.getBindings().size() == 0) {
			onAdd(null);
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		config.save(settings);
	}
}
