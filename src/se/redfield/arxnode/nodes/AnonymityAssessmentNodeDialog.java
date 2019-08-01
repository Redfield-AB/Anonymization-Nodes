package se.redfield.arxnode.nodes;

import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.AnonymityAssessmentNodeConfig;
import se.redfield.arxnode.ui.PopulationConfigPanel;

public class AnonymityAssessmentNodeDialog extends NodeDialogPane {
	private static final NodeLogger logger = NodeLogger.getLogger(AnonymityAssessmentNodeDialog.class);

	private AnonymityAssessmentNodeConfig config;

	private DialogComponentColumnFilter columnFilter;
	private PopulationConfigPanel population;

	public AnonymityAssessmentNodeDialog() {
		config = new AnonymityAssessmentNodeConfig();

		addTab("Settings", createSettingsPanel());
	}

	private JPanel createSettingsPanel() {
		columnFilter = new DialogComponentColumnFilter(config.getColumnFilter(), 0, false);
		columnFilter.setIncludeTitle("Quasi-identifying columns");

		DialogComponentNumber threshold = new DialogComponentNumber(config.getRiskThreshold(),
				"Re-identification Risk Threshold", 0.01);

		population = new PopulationConfigPanel(config.getPopulation());

		JPanel panel = new JPanel(new FormLayout("5:n, f:p:g, 5:n", "5:n, f:p:g, 5:n, p:n, 5:n, p:n"));
		panel.add(columnFilter.getComponentPanel(), CC.rc(2, 2));
		panel.add(threshold.getComponentPanel(), CC.rc(4, 2, "c, l"));
		panel.add(population, CC.rc(6, 2));
		return panel;
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		DataTableSpec spec = specs[HierarchyCreateNodeModel.PORT_DATA_TABLE];
		if ((spec == null) || (spec.getNumColumns() < 1)) {
			throw new NotConfigurableException("Cannot be configured without" + " input table");
		}

		try {
			config.load(settings);
		} catch (InvalidSettingsException e) {
			logger.warn(e.getMessage(), e);
		}

		columnFilter.loadSettingsFrom(settings, specs);
		population.loadFromConfig();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		config.save(settings);
	}
}
