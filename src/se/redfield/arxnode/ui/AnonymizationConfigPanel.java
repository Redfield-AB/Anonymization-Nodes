package se.redfield.arxnode.ui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable.Type;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.AnonymizationConfig;
import se.redfield.arxnode.nodes.AnonymizerNodeDialog;

public class AnonymizationConfigPanel {
	private static final NodeLogger logger = NodeLogger.getLogger(AnonymizationConfigPanel.class);

	private AnonymizerNodeDialog dlg;
	private AnonymizationConfig config;
	private JPanel component;
	private CellConstraints cc;

	private DialogComponentColumnNameSelection columnSelection;
	private PopulationConfigPanel populationPanel;

	public AnonymizationConfigPanel(AnonymizationConfig config, AnonymizerNodeDialog dlg) {
		this.config = config;
		this.dlg = dlg;
		initUi();
	}

	private void initUi() {
		cc = new CellConstraints();
		component = new JPanel(new FormLayout("f:p:g", "f:p:n, 5:n, f:p:n, 5:n, f:p:n, 5:n, f:p:n, 5:n, f:p:n"));
		component.add(createPartitioningPanel(), cc.rc(1, 1));
		component.add(createGeneralPanel(), cc.rc(3, 1));
		component.add(createSearchStrategyPanel(), cc.rc(5, 1));
		component.add(new MetricConfigPanel(config.getMeasure()), cc.rc(7, 1));
		component.add(populationPanel = new PopulationConfigPanel(config.getPopulation()), cc.rc(9, 1));
	}

	@SuppressWarnings("unchecked")
	private JPanel createPartitioningPanel() {
		columnSelection = new DialogComponentColumnNameSelection(config.getPartitionsGroupByColumn(), "Group by column",
				0, StringValue.class, DoubleValue.class);

		JPanel panel = new JPanel(new FormLayout("l:p:n, 5:n, l:p:g, p:g", "p:n, 5:n, p:n"));
		panel.add(
				new DialogComponentNumber(config.getNumOfThreads(), "# of threads", 1,
						dlg.createFlowVariableModel(new String[] { AnonymizationConfig.CONFIG_KEY,
								AnonymizationConfig.CONFIG_NUM_OF_THREADS }, Type.INTEGER)).getComponentPanel(),
				cc.rcw(1, 1, 3, "d,l"));
		panel.add(new DialogComponentBoolean(config.getPartitionsGroupByEnabled(), "").getComponentPanel(),
				cc.rc(3, 1));
		panel.add(columnSelection.getComponentPanel(), cc.rc(3, 3));
		panel.setBorder(BorderFactory.createTitledBorder("Partitioning"));
		return panel;
	}

	private JPanel createGeneralPanel() {
		JPanel panel = new JPanel(
				new FormLayout("l:p:n, p:g", "p:n, 5:n, p:n, 5:n, p:n, 5:n, p:n, 5:n, p:n, 5:n, p:n"));
		panel.add(
				new DialogComponentNumber(config.getSuppresionLimit(), "Suppression limit", 0.01,
						dlg.createFlowVariableModel(new String[] { AnonymizationConfig.CONFIG_KEY,
								AnonymizationConfig.CONFIG_SUPPRESSION_LIMIT }, Type.INTEGER)).getComponentPanel(),
				cc.rc(1, 1));
		panel.add(new DialogComponentBoolean(config.getPractivalMonotonicity(),
				"Approximate: assume practical monotonicity").getComponentPanel(), cc.rc(3, 1));
		panel.add(new DialogComponentNumber(config.getRiskThreshold(), "Re-identification Risk Threshold", 0.01)
				.getComponentPanel(), cc.rc(5, 1));
		panel.add(new DialogComponentBoolean(config.getAddClassColumn(), "Add Class column to output table")
				.getComponentPanel(), cc.rc(7, 1));
		panel.add(new DialogComponentBoolean(config.getOmitMissingValues(), "Omit rows with missing cells")
				.getComponentPanel(), cc.rc(9, 1));
		panel.add(new DialogComponentBoolean(config.getOmitIdentifyingColumns(), "Omit Identifying columns")
				.getComponentPanel(), cc.rc(11, 1));
		panel.setBorder(BorderFactory.createTitledBorder("General"));
		return panel;
	}

	private JPanel createSearchStrategyPanel() {
		JPanel panel = new JPanel(new FormLayout("l:p:n, 5:n, l:p:n, p:g", "p:n, 5:n, p:n, 5:n, p:n"));
		panel.add(new DialogComponentBoolean(config.getHeuristicSearchEnabled(), "Heuristic Search Enabled")
				.getComponentPanel(), cc.rcw(1, 1, 3, "default, left"));

		panel.add(new DialogComponentBoolean(config.getLimitSearchSteps(), "").getComponentPanel(), cc.rc(3, 1));
		panel.add(new DialogComponentNumber(config.getSearchStepsLimit(), "Limited number of steps", 100)
				.getComponentPanel(), cc.rc(3, 3));

		panel.add(new DialogComponentBoolean(config.getLimitSearchTime(), "").getComponentPanel(), cc.rc(5, 1));
		panel.add(new DialogComponentNumber(config.getSearchTimeLimit(), "Limited time [ms]", 100).getComponentPanel(),
				cc.rc(5, 3));

		panel.setBorder(BorderFactory.createTitledBorder("Search strategy"));
		return panel;
	}

	public JPanel getComponent() {
		return component;
	}

	public void load(NodeSettingsRO settings, PortObjectSpec[] specs) {
		try {
			columnSelection.loadSettingsFrom(settings, specs);
			populationPanel.loadFromConfig();
		} catch (NotConfigurableException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
