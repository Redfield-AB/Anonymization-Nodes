package se.redfield.arxnode.nodes;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.mahout.math.Arrays;
import org.deidentifier.arx.aggregates.StatisticsEquivalenceClasses;
import org.deidentifier.arx.risk.RiskModelSampleSummary.JournalistRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary.MarketerRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary.ProsecutorRisk;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.interactive.InteractiveNode;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.web.ValidationError;

import se.redfield.arxnode.anonymize.AnonymizationResult;
import se.redfield.arxnode.anonymize.AnonymizationResultProcessor;
import se.redfield.arxnode.anonymize.Anonymizer;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.nodes.AnonymizerNodeView.AnonymizerNodeViewValue;

public class AnonymizerNodeModel extends NodeModel
		implements InteractiveNode<AnonymizerNodeViewValue, AnonymizerNodeViewValue> {

	private static final NodeLogger logger = NodeLogger.getLogger(AnonymizerNodeModel.class);

	public static final int PORT_DATA_TABLE = 0;
	public static final int PORT_ARX = 1;

	private Config config;
	private AnonymizationResultProcessor outputBuilder;
	private Set<String> warnings;
	private List<AnonymizationResult> results;
	private int[] selectedTransformation;

	protected AnonymizerNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, ArxPortObject.TYPE_OPTIONAL }, new PortType[] {
				BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE, FlowVariablePortObject.TYPE });
		config = new Config();
		warnings = new HashSet<>();
	}

	public List<AnonymizationResult> getResults() {
		return results;
	}

	public int[] getSelectedTransformation() {
		return selectedTransformation;
	}

	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
		logger.debug("execute");
		try {
			if (results == null) {
				logger.debug("anonymizing");
				Anonymizer anonymizer = new Anonymizer(config);
				results = anonymizer.process((BufferedDataTable) inData[PORT_DATA_TABLE],
						(ArxPortObject) inData[PORT_ARX], exec);
			}
			logger.debug("processing result");
			return outputBuilder.process((BufferedDataTable) inData[PORT_DATA_TABLE], results, selectedTransformation,
					exec);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void reset() {
		logger.debug("reset");
		warnings.clear();
		results = null;
		selectedTransformation = null;
	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		logger.debug("configure");

		config.configure((DataTableSpec) inSpecs[PORT_DATA_TABLE], (ArxPortObjectSpec) inSpecs[PORT_ARX]);
		config.validate();

		outputBuilder = new AnonymizationResultProcessor(config, this);

		return new PortObjectSpec[] { outputBuilder.createOutDataTableSpec(), outputBuilder.createStatsTableSpec(),
				inSpecs[0], FlowVariablePortObjectSpec.INSTANCE };
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		logger.debug("saveSettingsTo");
		config.save(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		logger.debug("loadValidatedSettingsFrom");
		config.load(settings);
	}

	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		logger.debug("validateSettings");
		config.validate(settings);
	}

	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		// TODO load internal data.
		// Everything handed to output ports is loaded automatically (data
		// returned by the execute method, models loaded in loadModelContent,
		// and user settings set through loadSettingsFrom - is all taken care
		// of). Load here only the other internals that need to be restored
		// (e.g. data used by the views).

	}

	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		// TODO save internal models.
		// Everything written to output ports is saved automatically (data
		// returned by the execute method, models saved in the saveModelContent,
		// and user settings saved through saveSettingsTo - is all taken care
		// of). Save here only the other internals that need to be preserved
		// (e.g. data used by the views).

	}

	public void putVariables(double informationLoss, String headers, String transformation, String anonymity,
			long rowCount, long suppresedRecords, StatisticsEquivalenceClasses statistics, ProsecutorRisk prosecutor,
			JournalistRisk journalist, MarketerRisk marketer) {
		pushFlowVariableDouble("informationLoss", informationLoss);
		pushFlowVariableString("headers", headers);
		pushFlowVariableString("transformation", transformation);
		pushFlowVariableString("anonymity", anonymity);
		pushFlowVariableInt("rowCount", (int) rowCount);
		pushFlowVariableInt("suppresedRecords", (int) suppresedRecords);

		pushFlowVariableDouble("avgClassSize", statistics.getAverageEquivalenceClassSize());
		pushFlowVariableInt("minClassSize", statistics.getMinimalEquivalenceClassSize());
		pushFlowVariableInt("maxClassSize", statistics.getMaximalEquivalenceClassSize());
		pushFlowVariableInt("numberofClasses", statistics.getNumberOfEquivalenceClasses());
		pushFlowVariableDouble("avgClassSizeInclOutliers",
				statistics.getAverageEquivalenceClassSizeIncludingOutliers());
		pushFlowVariableInt("minClassSizeInclOutliers", statistics.getMinimalEquivalenceClassSizeIncludingOutliers());
		pushFlowVariableInt("maxClassSizeInclOutliers", statistics.getMaximalEquivalenceClassSizeIncludingOutliers());
		pushFlowVariableInt("numberofClassesInclOutliers", statistics.getNumberOfEquivalenceClassesIncludingOutliers());

		pushFlowVariableDouble("prosecutorRecordsAtRisk", prosecutor.getRecordsAtRisk());
		pushFlowVariableDouble("prosecutorHighestRisk", prosecutor.getHighestRisk());
		pushFlowVariableDouble("prosecutorSuccessRate", prosecutor.getSuccessRate());
		pushFlowVariableDouble("journalistRecordsAtRisk", journalist.getRecordsAtRisk());
		pushFlowVariableDouble("journalistHighestRisk", journalist.getHighestRisk());
		pushFlowVariableDouble("journalistSuccessRate", journalist.getSuccessRate());
		pushFlowVariableDouble("marketerSuccessRate", marketer.getSuccessRate());
	}

	public void showWarnig(String message) {
		if (!warnings.contains(message)) {
			warnings.add(message);
			String joined = StringUtils.join(warnings, ";\n");
			setWarningMessage(joined);
		}
	}

	@Override
	public AnonymizerNodeViewValue getViewRepresentation() {
		// TODO Auto-generated method stub
		logger.debug("getViewRepresentation");
		return null;
	}

	@Override
	public AnonymizerNodeViewValue getViewValue() {
		// TODO Auto-generated method stub
		logger.debug("getViewValue");
		return null;
	}

	@Override
	public ValidationError validateViewValue(AnonymizerNodeViewValue viewContent) {
		// TODO Auto-generated method stub
		logger.debug("validateViewValue");
		return null;
	}

	@Override
	public void loadViewValue(AnonymizerNodeViewValue viewContent, boolean useAsDefault) {
		// TODO Auto-generated method stub
		logger.debug("loadViewValue: " + Arrays.toString(viewContent.getTransformation()));
		selectedTransformation = viewContent.getTransformation();
	}
}
