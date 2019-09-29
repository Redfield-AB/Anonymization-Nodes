package se.redfield.arxnode.nodes;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
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
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, ArxPortObject.TYPE });
		config = new HierarchyExpandNodeConfig();
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
		if ((inSpecs[PORT_DATA_TABLE] == null) || (((DataTableSpec) inSpecs[PORT_DATA_TABLE]).getNumColumns() < 1)) {
			throw new InvalidSettingsException("Input table is missing or empty");
		}
		ArxPortObjectSpec inSpec = (ArxPortObjectSpec) inSpecs[PORT_ARX_OBJECT];
		validateFiles(inSpec);
		return new PortObjectSpec[] { inSpecs[PORT_DATA_TABLE], null, prepareSpec(inSpec) };
	}

	private void validateFiles(ArxPortObjectSpec inSpec) throws InvalidSettingsException {
		for (HierarchyBinding binding : config.getBindings()) {
			if (inSpec == null || !inSpec.getHierarchies().contains(binding.getColumnName())) {
				if (StringUtils.isEmpty(binding.getFileModel().getStringValue())) {
					throw new InvalidSettingsException("Hierarchy file is not set");
				}
				try {
					if (!binding.getFile().exists()) {
						throw new InvalidSettingsException("Hierarchy file does not exist");
					}
				} catch (IOException e) {
					throw new InvalidSettingsException(e);
				}
			}
		}
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
				(BufferedDataTable) inObjects[PORT_DATA_TABLE], (ArxPortObject) inObjects[PORT_ARX_OBJECT]);

		ArxPortObject out = ArxPortObject.create(outSpec, (ArxPortObject) inObjects[PORT_ARX_OBJECT]);
		out.getHierarchies().putAll(expandedHierarchies);

		BufferedDataTable preview = new HierarchyPreviewBuilder().build((BufferedDataTable) inObjects[PORT_DATA_TABLE],
				expandedHierarchies, exec);

		return new PortObject[] { inObjects[PORT_DATA_TABLE], preview, out };
	}

	private Map<String, HierarchyBuilder<?>> expandHierarchies(BufferedDataTable inTable, ArxPortObject inArxObject)
			throws IOException {
		if (inTable.size() <= 0) {
			throw new IllegalStateException("Input table is empty");
		}
		return HierarchyExpander.expand(inTable, config,
				inArxObject == null ? Collections.emptyMap() : inArxObject.getHierarchies());
	}
}
