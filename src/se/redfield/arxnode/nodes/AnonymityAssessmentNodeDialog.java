package se.redfield.arxnode.nodes;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;

import se.redfield.arxnode.config.AnonymityAssessmentNodeConfig;

public class AnonymityAssessmentNodeDialog extends DefaultNodeSettingsPane {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(AnonymityAssessmentNodeDialog.class);

	private AnonymityAssessmentNodeConfig config;

	public AnonymityAssessmentNodeDialog() {
		config = new AnonymityAssessmentNodeConfig();

		DialogComponentColumnFilter filter1 = new DialogComponentColumnFilter(config.getColumnFilter(), 0, false);
		filter1.setIncludeTitle("Quasi-identifying columns");
		addDialogComponent(filter1);
	}
}
