package se.redfield.arxnode.nodes;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import org.deidentifier.arx.aggregates.HierarchyBuilder;
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

import se.redfield.arxnode.config.HierarchyBinding;
import se.redfield.arxnode.config.HierarchyExpandNodeConfig;
import se.redfield.arxnode.hierarchy.HierarchyPreviewBuilder;
import se.redfield.arxnode.hierarchy.expand.HierarchyExpander;

public class HierarchyExpandNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyExpandNodeModel.class);

	public static final int PORT_DATA_TABLE = 0;
	public static final int PORT_ARX_OBJECT = 1;

	private HierarchyExpandNodeConfig config;
	private ArxPortObjectSpec outSpec;

	protected HierarchyExpandNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, ArxPortObject.TYPE_OPTIONAL },
				new PortType[] { BufferedDataTable.TYPE, ArxPortObject.TYPE, BufferedDataTable.TYPE });
		config = new HierarchyExpandNodeConfig();
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
		logger.debug("saveSettingsTo");
		config.save(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		logger.debug("validateSettings");
		HierarchyExpandNodeConfig temp = new HierarchyExpandNodeConfig();
		temp.load(settings);
		temp.validate();
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		logger.debug("loadSettingsFrom");
		config.load(settings);
	}

	@Override
	protected void reset() {
		logger.debug("reset");
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		logger.debug("configure");
		ArxPortObjectSpec inSpec = (ArxPortObjectSpec) inSpecs[PORT_ARX_OBJECT];
		return new PortObjectSpec[] { inSpecs[PORT_DATA_TABLE], prepareSpec(inSpec), null };
	}

	private ArxPortObjectSpec prepareSpec(ArxPortObjectSpec inSpec) {
		if (inSpec == null) {
			outSpec = new ArxPortObjectSpec();
		} else {
			outSpec = inSpec.clone();
		}
		outSpec.getHierarchies().addAll(
				config.getBindings().stream().map(HierarchyBinding::getColumnName).collect(Collectors.toList()));
		return outSpec;
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		logger.debug("execute");
		Map<String, HierarchyBuilder<?>> expandedHierarchies = expandHierarchies(
				(BufferedDataTable) inObjects[PORT_DATA_TABLE]);

		ArxPortObject out = ArxPortObject.create(outSpec, (ArxPortObject) inObjects[PORT_ARX_OBJECT]);
		out.getHierarchies().putAll(expandedHierarchies);

		BufferedDataTable preview = new HierarchyPreviewBuilder().build((BufferedDataTable) inObjects[PORT_DATA_TABLE],
				expandedHierarchies, exec);

		return new PortObject[] { inObjects[PORT_DATA_TABLE], out, preview };
	}

	private Map<String, HierarchyBuilder<?>> expandHierarchies(BufferedDataTable inTable) throws IOException {
		return HierarchyExpander.expand(inTable, config);
	}
}
