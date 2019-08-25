package se.redfield.arxnode.nodes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.web.ValidationError;
import org.knime.js.core.node.AbstractWizardNodeModel;

import se.redfield.arxnode.anonymize.AnonymizationResult;
import se.redfield.arxnode.anonymize.AnonymizationResultProcessor;
import se.redfield.arxnode.anonymize.Anonymizer;
import se.redfield.arxnode.config.Config;

public class AnonymizerJsNodeModel extends AbstractWizardNodeModel<AnonymizerJsNodeViewRep, AnonymizerJsNodeViewVal> {
	private static final NodeLogger logger = NodeLogger.getLogger(AnonymizerJsNodeModel.class);

	public static final int PORT_DATA_TABLE = 0;
	public static final int PORT_ARX = 1;

	private Config config;
	private Set<String> warnings;
	private AnonymizationResultProcessor outputBuilder;
	private List<AnonymizationResult> results;

	protected AnonymizerJsNodeModel(String viewName) {
		super(new PortType[] { BufferedDataTable.TYPE, ArxPortObject.TYPE_OPTIONAL },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE,
						BufferedDataTable.TYPE, FlowVariablePortObject.TYPE },
				viewName);
		logger.info("create JsModel: " + viewName);
		config = new Config();
		warnings = new HashSet<>();
	}

	@Override
	public AnonymizerJsNodeViewRep createEmptyViewRepresentation() {
		return new AnonymizerJsNodeViewRep();
	}

	@Override
	public AnonymizerJsNodeViewVal createEmptyViewValue() {
		return new AnonymizerJsNodeViewVal();
	}

	@Override
	public String getJavascriptObjectID() {
		return "se.redfield.arxnode.nodes.anonymizer";
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

	@Override
	public ValidationError validateViewValue(AnonymizerJsNodeViewVal viewContent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveCurrentValue(NodeSettingsWO content) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void performReset() {
		logger.debug("performReset");
		warnings.clear();
		results = null;

	}

	@Override
	protected void useCurrentValueAsDefault() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		config.save(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		Config tmp = new Config();
		tmp.load(settings);
		tmp.validate();
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		logger.info("loadSettings");
		config.load(settings);
	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		logger.debug("configure");

		config.configure((DataTableSpec) inSpecs[PORT_DATA_TABLE], (ArxPortObjectSpec) inSpecs[PORT_ARX]);
		config.validate();

		outputBuilder = new AnonymizationResultProcessor(config, null);

		return new PortObjectSpec[] { outputBuilder.createOutDataTableSpec(), outputBuilder.createStatsTableSpec(),
				inSpecs[0], outputBuilder.createRiskTableSpec(), FlowVariablePortObjectSpec.INSTANCE };
	}

	@Override
	protected PortObject[] performExecute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		logger.debug("performExecute");
		try {
			warnings.clear();
			if (results == null) {
				logger.debug("anonymizing");
				Anonymizer anonymizer = new Anonymizer(config);
				results = anonymizer.process((BufferedDataTable) inObjects[PORT_DATA_TABLE],
						(ArxPortObject) inObjects[PORT_ARX], exec);
				getViewRepresentation().updateFrom(results);
			}
			logger.debug("processing result");
			getViewValue().assignTo(results);
			getViewValue().updateFrom(results);
			return outputBuilder.process((BufferedDataTable) inObjects[PORT_DATA_TABLE], results, exec);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

}
