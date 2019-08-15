package se.redfield.arxnode.anonymize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.StatisticsEquivalenceClasses;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelAttributes.QuasiIdentifierRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary;
import org.deidentifier.arx.risk.RiskModelSampleSummary.JournalistRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary.MarketerRisk;
import org.deidentifier.arx.risk.RiskModelSampleSummary.ProsecutorRisk;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.nodes.AnonymizerNodeModel;
import se.redfield.arxnode.ui.transformation.InfolossScore;
import se.redfield.arxnode.util.RowClassifier;

public class AnonymizationResultProcessor {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(AnonymizationResultProcessor.class);

	private Config config;
	private AnonymizerNodeModel model;

	private Set<String> suppressedRows;

	public AnonymizationResultProcessor(Config config, AnonymizerNodeModel model) {
		this.config = config;
		this.model = model;
	}

	public PortObject[] process(BufferedDataTable inTable, List<AnonymizationResult> results, ExecutionContext exec)
			throws CanceledExecutionException {
		suppressedRows = new HashSet<>();
		return new PortObject[] { createDataTable(results, exec), createStatsTable(results, exec),
				createExceptionsTable(inTable, results, exec), createRiskTable(results, exec),
				FlowVariablePortObject.INSTANCE };
	}

	private BufferedDataTable createDataTable(List<AnonymizationResult> results, ExecutionContext exec) {
		List<ColumnConfig> outColumns = config.getOutputColumns();

		BufferedDataContainer container = exec.createDataContainer(createOutDataTableSpec());
		RowClassifier classifier = null;
		if (config.getAnonymizationConfig().getAddClassColumn().getBooleanValue()) {
			classifier = new RowClassifier(config);
		}
		boolean omitSuppressedRecords = config.getAnonymizationConfig().getOmitSuppressedRecords().getBooleanValue();

		for (AnonymizationResult r : results) {
			ARXResult res = r.getArxResult();
			if (res.isResultAvailable()) {
				DataHandle outHandle = res.getOutput(r.getCurrentNode());
				Iterator<String[]> iter = outHandle.iterator();
				iter.next();
				int rowIdx = -1;

				while (iter.hasNext()) {
					String[] row = iter.next();
					rowIdx += 1;

					if (omitSuppressedRecords && outHandle.isOutlier(rowIdx)) {
						continue;
					}

					List<DataCell> cells = new ArrayList<>();

					for (ColumnConfig c : outColumns) {
						cells.add(new StringCell(row[c.getIndex()]));
					}

					if (classifier != null) {
						cells.add(new IntCell(classifier.computeClass(row)));
					}

					RowKey rowKey = new RowKey(row[row.length - 1]);
					DataRow datarow = new DefaultRow(rowKey, cells);
					container.addRowToTable(datarow);
				}
			}
		}
		container.close();
		return container.getTable();
	}

	public DataTableSpec createOutDataTableSpec() {
		boolean classColumn = config.getAnonymizationConfig().getAddClassColumn().getBooleanValue();
		List<DataColumnSpec> specs = new ArrayList<>();

		for (ColumnConfig c : config.getOutputColumns()) {
			specs.add(new DataColumnSpecCreator(c.getName(), StringCell.TYPE).createSpec());
		}

		if (classColumn) {
			specs.add(new DataColumnSpecCreator("Class", IntCell.TYPE).createSpec());
		}

		return new DataTableSpec(specs.toArray(new DataColumnSpec[] {}));
	}

	private BufferedDataTable createStatsTable(List<AnonymizationResult> results, ExecutionContext exec) {
		BufferedDataContainer container = exec.createDataContainer(createStatsTableSpec());
		int row = 0;
		int totalSuppressedCount = 0;
		boolean flowVarsPushed = false;
		for (AnonymizationResult r : results) {
			ARXResult res = r.getArxResult();
			if (res.isResultAvailable()) {
				ARXNode opt = r.getCurrentNode();

				DataHandle handle = res.getOutput(opt);
				int suppresedRowsNum = 0;
				for (int i = 0; i < handle.getNumRows(); i++) {
					if (handle.isOutlier(i)) {
						suppressedRows.add(handle.getValue(i, handle.getNumColumns() - 1));
						suppresedRowsNum++;
					}
				}
				StatisticsEquivalenceClasses statistics = handle.getStatistics().getEquivalenceClassStatistics();

				totalSuppressedCount += suppresedRowsNum;
				InfolossScore minScore = InfolossScore.createFrom(r.getArxResult().getLattice(), opt.getLowestScore());
				InfolossScore maxScore = InfolossScore.createFrom(r.getArxResult().getLattice(), opt.getHighestScore());
				String headers = Arrays.toString(opt.getQuasiIdentifyingAttributes());
				String transformation = Arrays.toString(opt.getTransformation());
				String anonymity = opt.getAnonymity().toString();
				long rowCount = r.getPartitionInfo().getRows();

				RiskModelSampleSummary riskSummary = getRiskEstimator(r)
						.getSampleBasedRiskSummary(config.getAnonymizationConfig().getRiskThreshold().getDoubleValue());
				ProsecutorRisk prosecutorRisk = riskSummary.getProsecutorRisk();
				JournalistRisk journalistRisk = riskSummary.getJournalistRisk();
				MarketerRisk marketerRisk = riskSummary.getMarketerRisk();

				if (!flowVarsPushed) {
					flowVarsPushed = true;
					model.putVariables(minScore, maxScore, headers, transformation, anonymity, rowCount,
							suppresedRowsNum, statistics, prosecutorRisk, journalistRisk, marketerRisk);
				}

				List<DataCell> cells = new ArrayList<>();
				cells.add(new StringCell(minScore.getValue()));
				cells.add(new DoubleCell(minScore.getRelativePercent()));
				cells.add(new StringCell(maxScore.getValue()));
				cells.add(new DoubleCell(maxScore.getRelativePercent()));

				cells.add(new StringCell(headers));
				cells.add(new StringCell(transformation));
				cells.add(new StringCell(anonymity));
				cells.add(new LongCell(rowCount));
				cells.add(new StringCell(r.getPartitionInfo().getCriteria()));
				cells.add(new LongCell(suppresedRowsNum));

				cells.add(new DoubleCell(statistics.getAverageEquivalenceClassSize()));
				cells.add(new IntCell(statistics.getMinimalEquivalenceClassSize()));
				cells.add(new IntCell(statistics.getMaximalEquivalenceClassSize()));
				cells.add(new IntCell(statistics.getNumberOfEquivalenceClasses()));
				cells.add(new DoubleCell(statistics.getAverageEquivalenceClassSizeIncludingOutliers()));
				cells.add(new IntCell(statistics.getMinimalEquivalenceClassSizeIncludingOutliers()));
				cells.add(new IntCell(statistics.getMaximalEquivalenceClassSizeIncludingOutliers()));
				cells.add(new IntCell(statistics.getNumberOfEquivalenceClassesIncludingOutliers()));

				cells.add(new DoubleCell(prosecutorRisk.getRecordsAtRisk()));
				cells.add(new DoubleCell(prosecutorRisk.getHighestRisk()));
				cells.add(new DoubleCell(prosecutorRisk.getSuccessRate()));
				cells.add(new DoubleCell(journalistRisk.getRecordsAtRisk()));
				cells.add(new DoubleCell(journalistRisk.getHighestRisk()));
				cells.add(new DoubleCell(journalistRisk.getSuccessRate()));
				cells.add(new DoubleCell(marketerRisk.getSuccessRate()));

				RowKey key = new RowKey("Row" + row++);
				DataRow datarow = new DefaultRow(key, cells);
				container.addRowToTable(datarow);
			}
		}

		if (totalSuppressedCount > 0) {
			model.showWarnig("Some records were suppressed");
		}

		container.close();
		return container.getTable();
	}

	private RiskEstimateBuilder getRiskEstimator(AnonymizationResult r) {
		ARXNode node = r.getCurrentNode();
		return r.getArxResult().getOutput(node)
				.getRiskEstimator(config.getAnonymizationConfig().getPopulation().getPopulationModel());
	}

	public DataTableSpec createStatsTableSpec() {
		List<DataColumnSpec> cols = new ArrayList<>();
		cols.add(new DataColumnSpecCreator("Min Score", StringCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Min Score [%]", DoubleCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Max Score", StringCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Max Score [%]", DoubleCell.TYPE).createSpec());

		cols.add(new DataColumnSpecCreator("Headers", StringCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Transformation", StringCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Anonymity", StringCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Row count", LongCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Partition criteria", StringCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Suppressed records", LongCell.TYPE).createSpec());

		cols.add(new DataColumnSpecCreator("Average Class Size", DoubleCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Min Class Size", IntCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Max Class Size", IntCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Number of Classes", IntCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Average Class Size (incl. outliers)", DoubleCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Min Class Size  (incl. outliers)", IntCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Max Class Size  (incl. outliers)", IntCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Number of Classes (incl. outliers)", IntCell.TYPE).createSpec());

		cols.add(new DataColumnSpecCreator("Records at Risk [Prosecutor]", DoubleCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Highest Risk [Prosecutor]", DoubleCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Success Rate [Prosecutor]", DoubleCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Records at Risk [Journalist]", DoubleCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Highest Risk [Journalist]", DoubleCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Success Rate [Journalist]", DoubleCell.TYPE).createSpec());
		cols.add(new DataColumnSpecCreator("Success Rate [Marketer]", DoubleCell.TYPE).createSpec());

		return new DataTableSpec(cols.toArray(new DataColumnSpec[] {}));
	}

	private BufferedDataTable createExceptionsTable(BufferedDataTable inTable, List<AnonymizationResult> results,
			ExecutionContext exec) throws CanceledExecutionException {
		Set<String> ommitedRows = new HashSet<>();
		for (AnonymizationResult r : results) {
			ommitedRows.addAll(r.getPartitionInfo().getOmittedRows());
		}

		BufferedDataContainer suppressed = exec.createDataContainer(inTable.getDataTableSpec());
		BufferedDataContainer omitted = exec.createDataContainer(inTable.getDataTableSpec());

		CloseableRowIterator inIter = inTable.iterator();
		while (inIter.hasNext()) {
			DataRow inRow = inIter.next();
			if (suppressedRows.contains(inRow.getKey().getString())) {
				suppressed.addRowToTable(inRow);
			} else if (ommitedRows.contains(inRow.getKey().toString())) {
				omitted.addRowToTable(inRow);
			}

		}

		inIter.close();
		suppressed.close();
		omitted.close();

		return exec.createConcatenateTable(exec, suppressed.getTable(), omitted.getTable());
	}

	public DataTableSpec createRiskTableSpec() {
		DataColumnSpec[] outColSpecs = new DataColumnSpec[4];
		outColSpecs[0] = new DataColumnSpecCreator("Attribute", StringCell.TYPE).createSpec();
		outColSpecs[1] = new DataColumnSpecCreator("Distinction", DoubleCell.TYPE).createSpec();
		outColSpecs[2] = new DataColumnSpecCreator("Separation", DoubleCell.TYPE).createSpec();
		outColSpecs[3] = new DataColumnSpecCreator("Partition", IntCell.TYPE).createSpec();
		return new DataTableSpec(outColSpecs);
	}

	private BufferedDataTable createRiskTable(List<AnonymizationResult> results, ExecutionContext exec) {
		BufferedDataContainer container = exec.createDataContainer(createRiskTableSpec());
		int partition = 0;
		long rowIndex = 0;
		for (AnonymizationResult r : results) {
			if (r.getArxResult().isResultAvailable()) {
				QuasiIdentifierRisk[] risks = getRiskEstimator(r).getAttributeRisks().getAttributeRisks();
				for (QuasiIdentifierRisk risk : risks) {
					if (risk.getIdentifier().size() == 1) {
						List<DataCell> cells = new ArrayList<>();

						cells.add(new StringCell(risk.getIdentifier().get(0)));
						cells.add(new DoubleCell(risk.getDistinction()));
						cells.add(new DoubleCell(risk.getSeparation()));
						cells.add(new IntCell(partition));

						DataRow row = new DefaultRow(RowKey.createRowKey(rowIndex++), cells);
						container.addRowToTable(row);
					}
				}
			}
			partition++;
		}
		container.close();
		return container.getTable();
	}
}
