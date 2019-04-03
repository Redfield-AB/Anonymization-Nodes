package se.redfield.arxnode;

import java.io.File;
import java.io.IOException;

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

import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.nodes.ArxPortObject;
import se.redfield.arxnode.nodes.ArxPortObjectSpec;

public class ArxNodeNodeModel extends NodeModel {

	private static final NodeLogger logger = NodeLogger.getLogger(ArxNodeNodeModel.class);

	public static final int PORT_DATA_TABLE = 0;
	public static final int PORT_ARX = 1;

	private Config config;
	private Anonymizer anonymizer;

	protected ArxNodeNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, ArxPortObject.TYPE_OPTIONAL }, new PortType[] {
				BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE, FlowVariablePortObject.TYPE });
		config = new Config();
	}

	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
		try {
			return anonymizer.process((BufferedDataTable) inData[PORT_DATA_TABLE], (ArxPortObject) inData[PORT_ARX],
					exec);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void reset() {
		logger.debug("reset");
		if (anonymizer != null) {
			anonymizer.clear();
		}
	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		logger.debug("configure");

		config.configure((DataTableSpec) inSpecs[PORT_DATA_TABLE], (ArxPortObjectSpec) inSpecs[PORT_ARX]);
		config.validate();
		anonymizer = new Anonymizer(config, this);

		return new PortObjectSpec[] { anonymizer.createOutDataTableSpec(), anonymizer.createStatsTableSpec(),
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
			long rowCount, long suppresedRecords) {
		pushFlowVariableDouble("informationLoss", informationLoss);
		pushFlowVariableString("headers", headers);
		pushFlowVariableString("transformation", transformation);
		pushFlowVariableString("anonymity", anonymity);
		pushFlowVariableDouble("rowCount", rowCount);
		pushFlowVariableDouble("suppresedRecords", suppresedRecords);
	}
}
