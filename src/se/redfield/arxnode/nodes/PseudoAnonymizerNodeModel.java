package se.redfield.arxnode.nodes;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortType;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.anonymize.Pseudoanonymizer;
import se.redfield.arxnode.config.PseudoAnonymizerNodeConfig;

public class PseudoAnonymizerNodeModel extends NodeModel {
	public static final int PORT_DATA_TABLE = 0;

	private PseudoAnonymizerNodeConfig config;

	protected PseudoAnonymizerNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE }, new PortType[] { BufferedDataTable.TYPE });
		config = new PseudoAnonymizerNodeConfig();
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		config.save(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		PseudoAnonymizerNodeConfig temp = new PseudoAnonymizerNodeConfig();
		temp.load(settings);
		temp.validate();
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		config.load(settings);
	}

	@Override
	protected void reset() {
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		Utils.removeMissingColumns(config.getColumnFilter(), inSpecs[PORT_DATA_TABLE]);
		return new DataTableSpec[] {
				Pseudoanonymizer.create(config, inSpecs[PORT_DATA_TABLE]).getColumnRearranger().createSpec() };
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		Pseudoanonymizer worker = Pseudoanonymizer.create(config, inData[PORT_DATA_TABLE].getDataTableSpec());
		BufferedDataTable resultTable = worker.process(exec, inData[PORT_DATA_TABLE]);
		return new BufferedDataTable[] { resultTable };
	}

}
