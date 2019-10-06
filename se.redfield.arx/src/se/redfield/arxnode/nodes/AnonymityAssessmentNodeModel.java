/*
 * Copyright (c) 2019 Redfield AB.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *  
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package se.redfield.arxnode.nodes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.mahout.math.Arrays;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelAttributes;
import org.deidentifier.arx.risk.RiskModelAttributes.QuasiIdentifierRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary;
import org.deidentifier.arx.risk.RiskModelSampleSummary.MarketerRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary.ProsecutorRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary.RiskSummary;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.MissingCell;
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
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE });
		config = new AnonymityAssessmentNodeConfig();
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Node doesn't have any internals
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Node doesn't have any internals
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		config.save(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		AnonymityAssessmentNodeConfig temp = new AnonymityAssessmentNodeConfig();
		temp.load(settings);
		temp.validate();
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		config.load(settings);
	}

	@Override
	protected void reset() {
		// No data to reset
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		if (config.getColumnFilter().getIncludeList().isEmpty()) {
			config.getColumnFilter().setIncludeList(inSpecs[PORT_DATA_TABLE].getColumnNames());
			setWarningMessage("Autoconfigured: All columns are set as quasi-identifying");
		}

		config.removeMissingColumns(inSpecs[PORT_DATA_TABLE]);

		boolean hasSecondTable = inSpecs[PORT_ANONYMIZED_TABLE] != null;
		if (hasSecondTable) {
			checkSecondTable(inSpecs);
		}

		return new DataTableSpec[] { createQiTableSpec(hasSecondTable), createRiskTableSpec(hasSecondTable) };
	}

	private void checkSecondTable(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		List<String> qaColumns = config.getColumnFilter().getIncludeList();
		for (String column : qaColumns) {
			if (!inSpecs[PORT_ANONYMIZED_TABLE].containsName(column)) {
				throw new InvalidSettingsException("Anonymized table is missing column '" + column + "'");
			}
		}
	}

	private DataTableSpec createQiTableSpec(boolean secondTable) {
		List<DataColumnSpec> cols = new ArrayList<>();

		cols.add(new DataColumnSpecCreator("Attribute", StringCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Distinction", DoubleCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Separation", DoubleCell.TYPE).createSpec());

		if (secondTable) {
			cols.add(new DataColumnSpecCreator("Distinction - Anonymized", DoubleCell.TYPE).createSpec());
			cols.add(new DataColumnSpecCreator("Separation  - Anonymized", DoubleCell.TYPE).createSpec());
		}

		return new DataTableSpec(cols.toArray(new DataColumnSpec[] {}));
	}

	private DataTableSpec createRiskTableSpec(boolean secondTable) {
		List<DataColumnSpec> cols = new ArrayList<>();

		cols.add(new DataColumnSpecCreator("Attacker", StringCell.TYPE).createSpec());
		addRiskColumns(cols, "");
		if (secondTable) {
			addRiskColumns(cols, " - anonymized");
		}

		return new DataTableSpec(cols.toArray(new DataColumnSpec[] {}));
	}

	private void addRiskColumns(List<DataColumnSpec> cols, String titleSuffix) {
		cols.add(new DataColumnSpecCreator("Records at Risk" + titleSuffix, DoubleCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Highest Risk" + titleSuffix, DoubleCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Success Rate" + titleSuffix, DoubleCell.TYPE).createSpec());
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inObjects, ExecutionContext exec) throws Exception {
		return process(inObjects[PORT_DATA_TABLE], inObjects[PORT_ANONYMIZED_TABLE], exec);
	}

	private BufferedDataTable[] process(BufferedDataTable plain, BufferedDataTable anonymized, ExecutionContext exec)
			throws InterruptedException, ExecutionException, CanceledExecutionException {
		boolean hasSecondTable = anonymized != null;

		RiskEstimateBuilder riskEstimator = getRiskEstimator(readToData(plain));
		QuasiIdentifierRisk[] attrRisks = getAttributesRisks(riskEstimator.getInterruptibleInstance(), exec,
				hasSecondTable);
		RiskModelSampleSummary riskSummary = getRiskSummary(riskEstimator);

		RiskEstimateBuilder riskEstimator2 = null;
		QuasiIdentifierRisk[] attrRisks2 = null;
		RiskModelSampleSummary riskSummary2 = null;
		if (hasSecondTable) {
			riskEstimator2 = getRiskEstimator(readToData(anonymized));
			attrRisks2 = getAttributesRisks(riskEstimator2.getInterruptibleInstance(), exec, hasSecondTable);
			riskSummary2 = getRiskSummary(riskEstimator2);
		}

		BufferedDataContainer qiContainer = exec.createDataContainer(createQiTableSpec(hasSecondTable));
		for (int i = 0; i < attrRisks.length; i++) {
			QuasiIdentifierRisk risk = attrRisks[i];
			List<DataCell> cells = new ArrayList<>();

			cells.add(new StringCell(String.join(", ", risk.getIdentifier())));
			putDistinctionSeparation(cells, risk);

			if (hasSecondTable) {
				putDistinctionSeparation(cells, attrRisks2[i]);
			}

			Utils.addRow(qiContainer, cells, i);
		}
		qiContainer.close();

		BufferedDataContainer riskContainer = exec.createDataContainer(createRiskTableSpec(hasSecondTable));
		Utils.addRow(riskContainer, populateRiskRow(riskSummary.getProsecutorRisk(),
				hasSecondTable ? riskSummary2.getProsecutorRisk() : null), 0);
		Utils.addRow(riskContainer, populateRiskRow(riskSummary.getJournalistRisk(),
				hasSecondTable ? riskSummary2.getJournalistRisk() : null), 1);
		Utils.addRow(riskContainer, populateMarketerRiskRow(riskSummary.getMarketerRisk(),
				hasSecondTable ? riskSummary2.getMarketerRisk() : null), 2);
		riskContainer.close();

		return new BufferedDataTable[] { qiContainer.getTable(), riskContainer.getTable() };
	}

	private DefaultData readToData(BufferedDataTable inTable) {
		DefaultData defData = Data.create();

		List<String> columnNames = config.getColumnFilter().getIncludeList();
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

	private RiskEstimateBuilder getRiskEstimator(Data data) {
		return data.getHandle().getRiskEstimator(config.getPopulation().getPopulationModel());
	}

	private QuasiIdentifierRisk[] getAttributesRisks(RiskEstimateBuilderInterruptible riskEstimator,
			ExecutionContext exec, boolean hasSecondTable)
			throws InterruptedException, ExecutionException, CanceledExecutionException {
		ExecutionContext subContext = exec.createSubExecutionContext(hasSecondTable ? 0.5 : 1);

		CompletableFuture<RiskModelAttributes> future = CompletableFuture.supplyAsync(() -> {
			try {
				return riskEstimator.getAttributeRisks();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return null;
		});

		while (!future.isDone()) {
			try {
				Thread.sleep(100);
				exec.checkCanceled();
				subContext.setProgress(riskEstimator.getProgress() / 100.0);
			} catch (CanceledExecutionException | InterruptedException e) {
				riskEstimator.interrupt();
				throw e;
			}
		}
		return future.get().getAttributeRisks();
	}

	private RiskModelSampleSummary getRiskSummary(RiskEstimateBuilder riskEstimator) {
		return riskEstimator.getSampleBasedRiskSummary(config.getRiskThreshold().getDoubleValue());
	}

	private void putDistinctionSeparation(List<DataCell> cells, QuasiIdentifierRisk risk) {
		cells.add(new DoubleCell(risk.getDistinction()));
		cells.add(new DoubleCell(risk.getSeparation()));
	}

	private List<DataCell> populateRiskRow(RiskSummary risk1, RiskSummary risk2) {
		List<DataCell> cells = new ArrayList<>();

		String attacker = (risk1 instanceof ProsecutorRisk) ? "Prosecutor" : "Journalist";
		cells.add(new StringCell(attacker));

		putRiskCells(cells, risk1);
		if (risk2 != null) {
			putRiskCells(cells, risk2);
		}
		return cells;
	}

	private void putRiskCells(List<DataCell> cells, RiskSummary risk) {
		cells.add(new DoubleCell(risk.getRecordsAtRisk()));
		cells.add(new DoubleCell(risk.getHighestRisk()));
		cells.add(new DoubleCell(risk.getSuccessRate()));
	}

	private List<DataCell> populateMarketerRiskRow(MarketerRisk risk1, MarketerRisk risk2) {
		List<DataCell> cells = new ArrayList<>();
		cells.add(new StringCell("Marketer"));
		putMarketerRiskCells(cells, risk1);
		if (risk2 != null) {
			putMarketerRiskCells(cells, risk2);
		}
		return cells;
	}

	private void putMarketerRiskCells(List<DataCell> cells, MarketerRisk risk) {
		cells.add(new MissingCell(""));
		cells.add(new MissingCell(""));
		cells.add(new DoubleCell(risk.getSuccessRate()));
	}

}
