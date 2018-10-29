package se.redfield.arxnode.ui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.AnonymizationConfig;

public class AnonymizationConfigPanel {

	private AnonymizationConfig config;
	private JPanel component;
	private CellConstraints cc;

	public AnonymizationConfigPanel(AnonymizationConfig config) {
		this.config = config;
		initUi();
	}

	private void initUi() {
		cc = new CellConstraints();
		component = new JPanel(new FormLayout("f:p:g", "f:p:n, 5:n, f:p:n, 5:n, f:p:n"));
		component.add(createPartitioningPanel(), cc.rc(1, 1));
		component.add(createGeneralPanel(), cc.rc(3, 1));
		component.add(createSearchStrategyPanel(), cc.rc(5, 1));
	}

	private JPanel createPartitioningPanel() {
		JPanel panel = new JPanel(new FormLayout("l:p:n, p:g", "p:n, 5:n, p:n"));
		panel.add(new DialogComponentNumber(config.getNumOfThreads(), "# of threads", 1).getComponentPanel(),
				cc.rc(1, 1));
		panel.add(new DialogComponentBoolean(config.getPartitionsSingleOptimum(),
				"Try to use single transformation for all partitions").getComponentPanel(), cc.rc(3, 1));
		panel.setBorder(BorderFactory.createTitledBorder("Partitioning"));
		return panel;
	}

	private JPanel createGeneralPanel() {
		JPanel panel = new JPanel(new FormLayout("l:p:n, p:g", "p:n, 5:n, p:n, 5:n, p:n, 5:n, p:n"));
		panel.add(new DialogComponentNumber(config.getSuppresionLimit(), "Suppression limit", 0.01).getComponentPanel(),
				cc.rc(1, 1));
		panel.add(new DialogComponentBoolean(config.getPractivalMonotonicity(),
				"Approximate: assume practical monotonicity").getComponentPanel(), cc.rc(3, 1));
		panel.add(new DialogComponentBoolean(config.getPrecomputationEnabled(), "Enable precomputation")
				.getComponentPanel(), cc.rc(5, 1));
		panel.add(new DialogComponentNumber(config.getPrecomputationThreshold(), "Precomputation threshold", 0.01)
				.getComponentPanel(), cc.rc(7, 1));
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
}
