package se.redfield.arxnode.nodes;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.deidentifier.arx.aggregates.HierarchyBuilder;
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

import se.redfield.arxnode.config.HierarchyWriterNodeConfig;

public class HierarchyWriterNodeModel extends NodeModel {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyWriterNodeModel.class);

	private HierarchyWriterNodeConfig config;

	protected HierarchyWriterNodeModel() {
		super(new PortType[] { ArxPortObject.TYPE }, new PortType[] {});
		config = new HierarchyWriterNodeConfig();
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
		HierarchyWriterNodeConfig temp = new HierarchyWriterNodeConfig();
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
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		if (inSpecs.length == 0 || inSpecs[0] == null) {
			throw new InvalidSettingsException("No Arx Config Overrides input provided");
		}
		return new PortObjectSpec[] {};
	}

	@Override
	protected PortObject[] execute(PortObject[] inData, ExecutionContext exec) throws Exception {
		writeHierarchies((ArxPortObject) inData[0]);
		return new PortObject[] {};
	}

	private void writeHierarchies(ArxPortObject arxObject) throws InvalidPathException, IOException {
		File dir = config.getDirFile();
		boolean overwrite = config.getOverwrite().getBooleanValue();
		String nameFormat = config.getPrefix().getStringValue() + "%s.ahs";
		List<String> skipped = new ArrayList<>();

		for (Entry<String, HierarchyBuilder<?>> entry : arxObject.getHierarchies().entrySet()) {
			File file = new File(dir, String.format(nameFormat, entry.getKey()));
			if (!file.exists() || overwrite) {
				entry.getValue().save(file);
			} else {
				skipped.add(file.getAbsolutePath());
			}
		}

		if (skipped.size() > 0) {
			showSkippedWarning(skipped);
		}
	}

	private void showSkippedWarning(List<String> files) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Skipping existing file%s:", files.size() > 1 ? "s" : ""));
		for (String file : files) {
			sb.append("\n");
			sb.append(file);
		}
		setWarningMessage(sb.toString());
	}
}
