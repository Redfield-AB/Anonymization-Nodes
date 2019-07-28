package se.redfield.arxnode.nodes;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.web.ValidationError;
import org.knime.core.node.wizard.WizardNode;
import org.knime.core.node.wizard.WizardViewCreator;

import se.redfield.arxnode.anonymize.AnonymizationResult;
import se.redfield.arxnode.anonymize.AnonymizationResultProcessor;
import se.redfield.arxnode.anonymize.Anonymizer;
import se.redfield.arxnode.config.Config;

public class AnonymizerJsNodeModel extends NodeModel
		implements WizardNode<AnonymizerJsNodeViewRep, AnonymizerJsNodeViewVal> {
	private static final NodeLogger logger = NodeLogger.getLogger(AnonymizerJsNodeModel.class);

	public static final int PORT_DATA_TABLE = 0;
	public static final int PORT_ARX = 1;

	private Config config;
	private Set<String> warnings;
	private AnonymizationResultProcessor outputBuilder;
	private List<AnonymizationResult> results;

	protected AnonymizerJsNodeModel(int nrInDataPorts, int nrOutDataPorts) {
		super(new PortType[] { BufferedDataTable.TYPE, ArxPortObject.TYPE_OPTIONAL },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE,
						BufferedDataTable.TYPE, FlowVariablePortObject.TYPE });
		config = new Config();
		warnings = new HashSet<>();
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void reset() {
		logger.debug("reset");
		warnings.clear();
		results = null;
	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		logger.debug("configure");

		config.configure((DataTableSpec) inSpecs[PORT_DATA_TABLE], (ArxPortObjectSpec) inSpecs[PORT_ARX]);
		config.validate();

		// outputBuilder = new AnonymizationResultProcessor(config, this);

		return new PortObjectSpec[] { outputBuilder.createOutDataTableSpec(), outputBuilder.createStatsTableSpec(),
				inSpecs[0], outputBuilder.createRiskTableSpec(), FlowVariablePortObjectSpec.INSTANCE };
	}

	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
		logger.debug("execute");
		try {
			warnings.clear();
			if (results == null) {
				logger.debug("anonymizing");
				Anonymizer anonymizer = new Anonymizer(config);
				results = anonymizer.process((BufferedDataTable) inData[PORT_DATA_TABLE],
						(ArxPortObject) inData[PORT_ARX], exec);
			}
			logger.debug("processing result");

			return outputBuilder.process((BufferedDataTable) inData[PORT_DATA_TABLE], results, exec);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public ValidationError validateViewValue(AnonymizerJsNodeViewVal viewContent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadViewValue(AnonymizerJsNodeViewVal viewContent, boolean useAsDefault) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveCurrentValue(NodeSettingsWO content) {
		// TODO Auto-generated method stub

	}

	@Override
	public AnonymizerJsNodeViewRep getViewRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AnonymizerJsNodeViewVal getViewValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AnonymizerJsNodeViewRep createEmptyViewRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AnonymizerJsNodeViewVal createEmptyViewValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getJavascriptObjectID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getViewHTMLPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WizardViewCreator<AnonymizerJsNodeViewRep, AnonymizerJsNodeViewVal> getViewCreator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isHideInWizard() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHideInWizard(boolean hide) {
		// TODO Auto-generated method stub

	}

}
