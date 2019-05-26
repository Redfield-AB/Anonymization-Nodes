package se.redfield.arxnode.nodes;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
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

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.config.HierarchyCreateNodeConfig;

public class HierarchyCreateNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyCreateNodeModel.class);

	public static final int PORT_DATA_TABLE = 0;
	public static final int PORT_ARX_OBJECT = 1;

	private HierarchyCreateNodeConfig config;
	private ArxPortObjectSpec outSpec;

	protected HierarchyCreateNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, ArxPortObject.TYPE_OPTIONAL },
				new PortType[] { BufferedDataTable.TYPE, ArxPortObject.TYPE, BufferedDataTable.TYPE });
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
		return new PortObjectSpec[] { inSpecs[PORT_DATA_TABLE], outSpec, null };
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		ArxPortObject out = ArxPortObject.create(outSpec, (ArxPortObject) inObjects[PORT_ARX_OBJECT]);
		out.getHierarchies().put(config.getColumnName(), config.getBuilder());
		return new PortObject[] { inObjects[PORT_DATA_TABLE], out,
				createPreviewTable(config.getBuilder(), (BufferedDataTable) inObjects[PORT_DATA_TABLE], exec) };
	}

	private BufferedDataTable createPreviewTable(HierarchyBuilder<?> hb, BufferedDataTable inTable,
			ExecutionContext exec) throws IOException {

		Set<String> data = new HashSet<>();
		int idx = inTable.getDataTableSpec().findColumnIndex(config.getColumnName());
		for (DataRow row : inTable) {
			data.add(Utils.toString(row.getCell(idx)));
		}

		String[][] preview = Utils.clone(hb).build(data.toArray(new String[] {})).getHierarchy();
		if (preview == null || preview.length == 0 || preview[0] == null || preview[0].length == 0) {
			logger.warn("Preview data is empty");
			return null;
		}

		int columnCount = preview[0].length;
		DataColumnSpec[] specs = new DataColumnSpec[columnCount];
		for (int i = 0; i < specs.length; i++) {
			specs[i] = new DataColumnSpecCreator("Level-" + i, StringCell.TYPE).createSpec();
		}
		BufferedDataContainer container = exec.createDataContainer(new DataTableSpec(specs));

		for (int i = 0; i < preview.length; i++) {
			DataCell[] cells = new DataCell[columnCount];

			for (int j = 0; j < columnCount; j++) {
				cells[j] = new StringCell(preview[i][j]);
			}

			RowKey rowKey = RowKey.createRowKey((long) i);
			DataRow row = new DefaultRow(rowKey, cells);
			container.addRowToTable(row);
		}

		container.close();
		return container.getTable();
	}

}
