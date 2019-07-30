package se.redfield.arxnode.nodes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.mahout.math.Arrays;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.risk.RiskModelAttributes.QuasiIdentifierRisk;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
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
import org.knime.core.node.port.PortType;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.config.AnonymityAssessmentNodeConfig;

public class AnonymityAssessmentNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(AnonymityAssessmentNodeModel.class);

	public static final int PORT_DATA_TABLE = 0;
	public static final int PORT_ANONYMIZED_TABLE = 1;

	private AnonymityAssessmentNodeConfig config;

	protected AnonymityAssessmentNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE_OPTIONAL },
				new PortType[] { BufferedDataTable.TYPE });
		config = new AnonymityAssessmentNodeConfig();
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
		if (config.getColumnFilter().getIncludeList().isEmpty()) {
			config.getColumnFilter().setIncludeList(inSpecs[PORT_DATA_TABLE].getColumnNames());
			setWarningMessage("Autoconfigured: All columns are set as quasi-identifying");
		}

		if (inSpecs[PORT_ANONYMIZED_TABLE] != null) {
			checkSecondTable(inSpecs);
		}

		return new DataTableSpec[] { createOutSpec(inSpecs[PORT_ANONYMIZED_TABLE] != null) };
	}

	private void checkSecondTable(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		List<String> qaColumns = getQAColumns(inSpecs[PORT_DATA_TABLE]);
		for (String column : qaColumns) {
			if (!inSpecs[PORT_ANONYMIZED_TABLE].containsName(column)) {
				throw new InvalidSettingsException("Anonymized table is missing column '" + column + "'");
			}
		}
	}

	private DataTableSpec createOutSpec(boolean secondTable) {
		List<DataColumnSpec> cols = new ArrayList<>();

		cols.add(new DataColumnSpecCreator("Attribute", StringCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Distinction", DoubleCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Separation", DoubleCell.TYPE).createSpec());

		if (secondTable) {
			cols.add(new DataColumnSpecCreator("Distinction [Anonymized]", DoubleCell.TYPE).createSpec());
			cols.add(new DataColumnSpecCreator("Separation [Anonymized]", DoubleCell.TYPE).createSpec());
		}

		DataTableSpec spec = new DataTableSpec(cols.toArray(new DataColumnSpec[] {}));
		return spec;
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inObjects, ExecutionContext exec) throws Exception {
		return new BufferedDataTable[] { process(inObjects[PORT_DATA_TABLE], inObjects[PORT_ANONYMIZED_TABLE], exec) };
	}

	private BufferedDataTable process(BufferedDataTable plain, BufferedDataTable anonymized, ExecutionContext exec) {
		boolean hasSecondTable = anonymized != null;

		DefaultData data = readToData(plain);
		QuasiIdentifierRisk[] attrRisks = data.getHandle().getRiskEstimator().getAttributeRisks().getAttributeRisks();

		QuasiIdentifierRisk[] attrRisksSecond = null;
		if (hasSecondTable) {
			attrRisksSecond = readToData(anonymized).getHandle().getRiskEstimator().getAttributeRisks()
					.getAttributeRisks();
		}

		BufferedDataContainer container = exec.createDataContainer(createOutSpec(hasSecondTable));
		for (int i = 0; i < attrRisks.length; i++) {
			QuasiIdentifierRisk risk = attrRisks[i];
			List<DataCell> cells = new ArrayList<>();

			cells.add(new StringCell(Arrays.toString(risk.getIdentifier().toArray())));
			cells.add(new DoubleCell(risk.getDistinction()));
			cells.add(new DoubleCell(risk.getSeparation()));

			if (hasSecondTable) {
				QuasiIdentifierRisk risk2 = attrRisksSecond[i];
				cells.add(new DoubleCell(risk2.getDistinction()));
				cells.add(new DoubleCell(risk2.getSeparation()));
			}

			DataRow row = new DefaultRow(RowKey.createRowKey((long) i), cells);
			container.addRowToTable(row);
		}
		container.close();
		return container.getTable();
	}

	private DefaultData readToData(BufferedDataTable inTable) {
		DefaultData defData = Data.create();

		List<String> columnNames = getQAColumns(inTable.getDataTableSpec());
		List<Integer> columnIndexes = columnNames.stream().map(name -> inTable.getDataTableSpec().findColumnIndex(name))
				.collect(Collectors.toList());
		logger.debug("qa columns: " + Arrays.toString(columnNames.toArray()));

		defData.add(columnNames.toArray(new String[] {}));
		for (String name : columnNames) {
			defData.getDefinition().setDataType(name, DataType.STRING);
			defData.getDefinition().setAttributeType(name, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
		}

		for (DataRow dataRow : inTable) {
			List<String> row = new ArrayList<>();
			for (Integer idx : columnIndexes) {
				row.add(Utils.toString(dataRow.getCell(idx)));
			}
			defData.add(row.toArray(new String[] {}));
		}

		return defData;
	}

	private List<String> getQAColumns(DataTableSpec spec) {
		List<String> columns = new ArrayList<>(config.getColumnFilter().getIncludeList());
		Iterator<String> iter = columns.iterator();
		while (iter.hasNext()) {
			String c = iter.next();
			if (!spec.containsName(c)) {
				logger.warn("Skipping missing column: " + c);
				iter.remove();
			}
		}
		return columns;
	}

}
