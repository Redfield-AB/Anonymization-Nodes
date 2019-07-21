package se.redfield.arxnode.nodes;

import java.io.File;
import java.io.IOException;

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

import se.redfield.arxnode.config.HierarchyCreateNodeConfig;
import se.redfield.arxnode.hierarchy.HierarchyPreviewBuilder;

public class HierarchyCreateNodeModel extends NodeModel {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyCreateNodeModel.class);

	public static final int PORT_DATA_TABLE = 0;
	public static final int PORT_ARX_OBJECT = 1;

	private HierarchyCreateNodeConfig config;
	private ArxPortObjectSpec outSpec;

	protected HierarchyCreateNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, ArxPortObject.TYPE_OPTIONAL },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, ArxPortObject.TYPE });
		config = new HierarchyCreateNodeConfig();
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
		config.save(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		HierarchyCreateNodeConfig temp = new HierarchyCreateNodeConfig();
		temp.load(settings);
		temp.validate();
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		config.load(settings);
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		if (config.getBuilder() == null) {
			throw new InvalidSettingsException("Hierarchy is not initialized");
		}
		ArxPortObjectSpec inSpec = (ArxPortObjectSpec) inSpecs[PORT_ARX_OBJECT];
		if (inSpec == null) {
			outSpec = new ArxPortObjectSpec();
		} else {
			outSpec = inSpec.clone();
		}
		outSpec.getHierarchies().add(config.getColumnName());
		return new PortObjectSpec[] { inSpecs[PORT_DATA_TABLE], null, outSpec };
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		ArxPortObject out = ArxPortObject.create(outSpec, (ArxPortObject) inObjects[PORT_ARX_OBJECT]);
		out.getHierarchies().put(config.getColumnName(), config.getBuilder());

		BufferedDataTable preview = new HierarchyPreviewBuilder().build((BufferedDataTable) inObjects[PORT_DATA_TABLE],
				config.getColumnName(), config.getBuilder(), exec);

		return new PortObject[] { inObjects[PORT_DATA_TABLE], preview, out };
	}

}
