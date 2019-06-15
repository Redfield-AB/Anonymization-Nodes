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
import se.redfield.arxnode.util.RowClassifier;

public class AnonymizationResultProcessor {
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
				createExceptionsTable(inTable, results, exec), FlowVariablePortObject.INSTANCE };
	}

	private BufferedDataTable createDataTable(List<AnonymizationResult> results, ExecutionContext exec) {
		List<ColumnConfig> outColumns = config.getOutputColumns();

		BufferedDataContainer container = exec.createDataContainer(createOutDataTableSpec());
		RowClassifier classifier = null;
		if (config.getAnonymizationConfig().getAddClassColumn().getBooleanValue()) {
			classifier = new RowClassifier(config);
		}

		for (AnonymizationResult r : results) {
			ARXResult res = r.getArxResult();
			if (res.isResultAvailable()) {
				Iterator<String[]> iter = res.getOutput(r.getCurrentNode()).iterator();
				iter.next();
				while (iter.hasNext()) {
					String[] row = iter.next();
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
				double informationLoss = Double.valueOf(opt.getHighestScore().toString());
				String headers = Arrays.toString(opt.getQuasiIdentifyingAttributes());
				String transformation = Arrays.toString(opt.getTransformation());
				String anonymity = opt.getAnonymity().toString();
				long rowCount = r.getPartitionInfo().getRows();

				RiskModelSampleSummary riskSummary = handle.getRiskEstimator()
						.getSampleBasedRiskSummary(config.getAnonymizationConfig().getRiskThreshold().getDoubleValue());
				ProsecutorRisk prosecutorRisk = riskSummary.getProsecutorRisk();
				JournalistRisk journalistRisk = riskSummary.getJournalistRisk();
				MarketerRisk marketerRisk = riskSummary.getMarketerRisk();

				if (!flowVarsPushed) {
					flowVarsPushed = true;
					model.putVariables(informationLoss, headers, transformation, anonymity, rowCount, suppresedRowsNum,
							statistics, prosecutorRisk, journalistRisk, marketerRisk);
				}

				DataCell[] cells = new DataCell[22];
				cells[0] = new DoubleCell(informationLoss);
				cells[1] = new StringCell(headers);
				cells[2] = new StringCell(transformation);
				cells[3] = new StringCell(anonymity);
				cells[4] = new LongCell(rowCount);
				cells[5] = new StringCell(r.getPartitionInfo().getCriteria());
				cells[6] = new LongCell(suppresedRowsNum);

				cells[7] = new DoubleCell(statistics.getAverageEquivalenceClassSize());
				cells[8] = new IntCell(statistics.getMinimalEquivalenceClassSize());
				cells[9] = new IntCell(statistics.getMaximalEquivalenceClassSize());
				cells[10] = new IntCell(statistics.getNumberOfEquivalenceClasses());
				cells[11] = new DoubleCell(statistics.getAverageEquivalenceClassSizeIncludingOutliers());
				cells[12] = new IntCell(statistics.getMinimalEquivalenceClassSizeIncludingOutliers());
				cells[13] = new IntCell(statistics.getMaximalEquivalenceClassSizeIncludingOutliers());
				cells[14] = new IntCell(statistics.getNumberOfEquivalenceClassesIncludingOutliers());

				cells[15] = new DoubleCell(prosecutorRisk.getRecordsAtRisk());
				cells[16] = new DoubleCell(prosecutorRisk.getHighestRisk());
				cells[17] = new DoubleCell(prosecutorRisk.getSuccessRate());
				cells[18] = new DoubleCell(journalistRisk.getRecordsAtRisk());
				cells[19] = new DoubleCell(journalistRisk.getHighestRisk());
				cells[20] = new DoubleCell(journalistRisk.getSuccessRate());
				cells[21] = new DoubleCell(marketerRisk.getSuccessRate());

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

	public DataTableSpec createStatsTableSpec() {
		DataColumnSpec[] outColSpecs = new DataColumnSpec[22];
		outColSpecs[0] = new DataColumnSpecCreator("Information Loss", DoubleCell.TYPE).createSpec();
		outColSpecs[1] = new DataColumnSpecCreator("Headers", StringCell.TYPE).createSpec();
		outColSpecs[2] = new DataColumnSpecCreator("Transformation", StringCell.TYPE).createSpec();
		outColSpecs[3] = new DataColumnSpecCreator("Anonymity", StringCell.TYPE).createSpec();
		outColSpecs[4] = new DataColumnSpecCreator("Row count", LongCell.TYPE).createSpec();
		outColSpecs[5] = new DataColumnSpecCreator("Partition criteria", StringCell.TYPE).createSpec();
		outColSpecs[6] = new DataColumnSpecCreator("Suppressed records", LongCell.TYPE).createSpec();

		outColSpecs[7] = new DataColumnSpecCreator("Average Class Size", DoubleCell.TYPE).createSpec();
		outColSpecs[8] = new DataColumnSpecCreator("Min Class Size", IntCell.TYPE).createSpec();
		outColSpecs[9] = new DataColumnSpecCreator("Max Class Size", IntCell.TYPE).createSpec();
		outColSpecs[10] = new DataColumnSpecCreator("Number of Classes", IntCell.TYPE).createSpec();
		outColSpecs[11] = new DataColumnSpecCreator("Average Class Size (incl. outliers)", DoubleCell.TYPE)
				.createSpec();
		outColSpecs[12] = new DataColumnSpecCreator("Min Class Size  (incl. outliers)", IntCell.TYPE).createSpec();
		outColSpecs[13] = new DataColumnSpecCreator("Max Class Size  (incl. outliers)", IntCell.TYPE).createSpec();
		outColSpecs[14] = new DataColumnSpecCreator("Number of Classes (incl. outliers)", IntCell.TYPE).createSpec();

		outColSpecs[15] = new DataColumnSpecCreator("Records at Risk [Prosecutor]", DoubleCell.TYPE).createSpec();
		outColSpecs[16] = new DataColumnSpecCreator("Highest Risk [Prosecutor]", DoubleCell.TYPE).createSpec();
		outColSpecs[17] = new DataColumnSpecCreator("Success Rate [Prosecutor]", DoubleCell.TYPE).createSpec();
		outColSpecs[18] = new DataColumnSpecCreator("Records at Risk [Journalist]", DoubleCell.TYPE).createSpec();
		outColSpecs[19] = new DataColumnSpecCreator("Highest Risk [Journalist]", DoubleCell.TYPE).createSpec();
		outColSpecs[20] = new DataColumnSpecCreator("Success Rate [Journalist]", DoubleCell.TYPE).createSpec();
		outColSpecs[21] = new DataColumnSpecCreator("Success Rate [Marketer]", DoubleCell.TYPE).createSpec();

		return new DataTableSpec(outColSpecs);
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
}
