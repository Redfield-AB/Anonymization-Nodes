package se.redfield.arxnode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.MicroAggregationFunction;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
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
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.util.Pair;

import se.redfield.arxnode.config.AnonymizationConfig;
import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.config.TransformationConfig;
import se.redfield.arxnode.config.TransformationConfig.Mode;
import se.redfield.arxnode.nodes.AnonymizerNodeModel;
import se.redfield.arxnode.nodes.ArxPortObject;
import se.redfield.arxnode.partiton.PartitionInfo;
import se.redfield.arxnode.partiton.Partitioner;
import se.redfield.arxnode.util.RowClassifier;

public class Anonymizer {

	private static final NodeLogger logger = NodeLogger.getLogger(Anonymizer.class);

	private Config config;
	private AnonymizerNodeModel model;
	private ARXNode optimum;
	private ArxPortObject arxPortObject;

	public Anonymizer(Config config, AnonymizerNodeModel model) {
		this.config = config;
		this.model = model;
	}

	public PortObject[] process(BufferedDataTable inTable, ArxPortObject arxObject, ExecutionContext exec)
			throws Exception {
		this.arxPortObject = arxObject;
		AnonymizationConfig anonConfig = config.getAnonymizationConfig();
		Partitioner partitioner = Partitioner.createPartitioner(anonConfig.getNumOfThreads().getIntValue(),
				anonConfig.getPartitionsGroupByEnabled().getBooleanValue()
						? anonConfig.getPartitionsGroupByColumn().getStringValue()
						: "",
				inTable);
		List<Pair<DefaultData, PartitionInfo>> parts = partitioner.partition(inTable);

		ExecutorService executor = Executors.newFixedThreadPool(parts.size());
		CompletionService<Pair<ARXResult, PartitionInfo>> service = new ExecutorCompletionService<>(executor);
		for (Pair<DefaultData, PartitionInfo> pair : parts) {
			ARXConfiguration arxConfig = configure(pair.getFirst());
			service.submit(() -> {
				ARXAnonymizer anonymizer = new ARXAnonymizer();
				ARXResult result = anonymizer.anonymize(pair.getFirst(), arxConfig);
				return new Pair<ARXResult, PartitionInfo>(result, pair.getSecond());
			});
		}
		int received = 0;
		List<Pair<ARXResult, PartitionInfo>> results = new ArrayList<>();
		while (received++ < parts.size()) {
			try {
				results.add(service.take().get());
			} catch (InterruptedException | ExecutionException e) {
				executor.shutdownNow();
				throw e;
			}
		}
		executor.shutdown();

		optimum = findSingleOptimum(results);
		BufferedDataTable anonTable = createDataTable(inTable, results, exec);
		return new PortObject[] { anonTable, createStatsTable(results, exec),
				createExceptionsTable(inTable, anonTable, exec), FlowVariablePortObject.INSTANCE };
	}

	private BufferedDataTable createExceptionsTable(BufferedDataTable inTable, BufferedDataTable outTable,
			ExecutionContext exec) {
		BufferedDataContainer container = exec.createDataContainer(inTable.getDataTableSpec());
		CloseableRowIterator outIter = outTable.iterator();
		CloseableRowIterator inIter = inTable.iterator();
		while (outIter.hasNext()) {
			DataRow outRow = outIter.next();
			DataRow inRow = inIter.next();

			boolean sameOrErased = true;
			for (int i = 0; i < inRow.getNumCells() && sameOrErased; i++) {
				String val = outRow.getCell(i).toString();
				sameOrErased = val.equals("*") || val.equals(inRow.getCell(i).toString());
			}

			if (sameOrErased) {
				container.addRowToTable(inRow);
			}
		}
		container.close();
		inIter.close();
		outIter.close();
		return container.getTable();
	}

	private ARXNode findSingleOptimum(List<Pair<ARXResult, PartitionInfo>> results) {
		if (config.getAnonymizationConfig().getPartitionsSingleOptimum().getBooleanValue()) {
			return results.stream().map(Pair::getFirst).filter(ARXResult::isResultAvailable)
					.map(ARXResult::getGlobalOptimum).findFirst().orElse(null);
		}
		return null;
	}

	private BufferedDataTable createStatsTable(List<Pair<ARXResult, PartitionInfo>> results, ExecutionContext exec) {
		BufferedDataContainer container = exec.createDataContainer(createStatsTableSpec());
		int row = 0;
		int totalSuppressedCount = 0;
		boolean flowVarsPushed = false;
		for (Pair<ARXResult, PartitionInfo> pair : results) {
			ARXResult res = pair.getFirst();
			if (res.isResultAvailable()) {
				ARXNode opt = findOptimumNode(res);

				DataHandle handle = res.getOutput(opt);
				int suppresedRowsNum = 0;
				for (int i = 0; i < handle.getNumRows(); i++) {
					if (handle.isOutlier(i)) {
						suppresedRowsNum++;
					}
				}
				StatisticsEquivalenceClasses statistics = handle.getStatistics().getEquivalenceClassStatistics();

				totalSuppressedCount += suppresedRowsNum;
				double informationLoss = Double.valueOf(opt.getHighestScore().toString());
				String headers = Arrays.toString(opt.getQuasiIdentifyingAttributes());
				String transformation = Arrays.toString(opt.getTransformation());
				String anonymity = opt.getAnonymity().toString();
				long rowCount = pair.getSecond().getRows();

				RiskModelSampleSummary riskSummary = handle.getRiskEstimator()
						.getSampleBasedRiskSummary(config.getAnonymizationConfig().getRiskThreshold().getDoubleValue());
				ProsecutorRisk prosecutorRisk = riskSummary.getProsecutorRisk();
				JournalistRisk journalistRisk = riskSummary.getJournalistRisk();
				MarketerRisk marketerRisk = riskSummary.getMarketerRisk();

				if (!flowVarsPushed) {
					flowVarsPushed = true;
					model.putVariables(informationLoss, headers, transformation, anonymity, rowCount, suppresedRowsNum);
				}

				DataCell[] cells = new DataCell[22];
				cells[0] = new DoubleCell(informationLoss);
				cells[1] = new StringCell(headers);
				cells[2] = new StringCell(transformation);
				cells[3] = new StringCell(anonymity);
				cells[4] = new LongCell(rowCount);
				cells[5] = new StringCell(pair.getSecond().getCriteria());
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

	private BufferedDataTable createDataTable(BufferedDataTable inTable, List<Pair<ARXResult, PartitionInfo>> results,
			ExecutionContext exec) {
		BufferedDataContainer container = exec.createDataContainer(createOutDataTableSpec());
		CloseableRowIterator inIterator = inTable.iterator();
		for (Pair<ARXResult, PartitionInfo> pair : results) {
			ARXResult res = pair.getFirst();
			if (res.isResultAvailable()) {
				RowClassifier classifier = null;
				if (config.getAnonymizationConfig().getAddClassColumn().getBooleanValue()) {
					classifier = new RowClassifier(config);
				}

				Iterator<String[]> iter = res.getOutput(findOptimumNode(res)).iterator();
				iter.next();
				while (iter.hasNext()) {
					String[] row = iter.next();

					DataCell[] cells = new DataCell[classifier == null ? row.length : row.length + 1];
					for (int i = 0; i < row.length; i++) {
						cells[i] = new StringCell(row[i]);
					}

					if (classifier != null) {
						cells[cells.length - 1] = new IntCell(classifier.computeClass(row));
					}

					DataRow inRow = inIterator.next();
					DataRow datarow = new DefaultRow(inRow.getKey(), cells);
					container.addRowToTable(datarow);
				}
			} else {
				for (int i = 0; i < pair.getSecond().getRows(); i++) {
					inIterator.next();
				}
			}
		}
		container.close();
		inIterator.close();
		return container.getTable();
	}

	public DataTableSpec createOutDataTableSpec() {
		boolean classColumn = config.getAnonymizationConfig().getAddClassColumn().getBooleanValue();
		DataColumnSpec[] outColSpecs = new DataColumnSpec[classColumn ? config.getColumns().size() + 1
				: config.getColumns().size()];
		config.getColumns().forEach(c -> {
			outColSpecs[c.getIndex()] = new DataColumnSpecCreator(c.getName(), StringCell.TYPE).createSpec();
		});

		if (classColumn) {
			outColSpecs[outColSpecs.length - 1] = new DataColumnSpecCreator("Class", IntCell.TYPE).createSpec();
		}

		return new DataTableSpec(outColSpecs);
	}

	private ARXNode findOptimumNode(ARXResult res) {
		if (optimum == null) {
			return res.getGlobalOptimum();
		}
		int[] levels = optimum.getTransformation();
		ARXNode opt = res.getGlobalOptimum();
		for (ARXNode[] nodes : res.getLattice().getLevels()) {
			for (ARXNode n : nodes) {
				if (Arrays.equals(levels, n.getTransformation())) {
					return n;
				}
			}
		}
		model.showWarnig("Unable to use a single transformation for all partitions");
		return opt;
	}

	private ARXConfiguration configure(Data defData) {
		config.getColumns().forEach(c -> {
			DataDefinition def = defData.getDefinition();
			def.setAttributeType(c.getName(), c.getAttrType());

			if (c.getAttrType() == AttributeType.QUASI_IDENTIFYING_ATTRIBUTE) {
				HierarchyBuilder<?> hierarchy = getHierarchy(c);
				def.setAttributeType(c.getName(), hierarchy);

				TransformationConfig tc = c.getTransformationConfig();
				if (tc.getMode() == Mode.GENERALIZATION) {
					if (tc.getMinGeneralization() != null) {
						def.setMinimumGeneralization(c.getName(), tc.getMinGeneralization());
					}
					if (tc.getMaxGeneralization() != null) {
						def.setMaximumGeneralization(c.getName(), tc.getMaxGeneralization());
					}
				} else {
					boolean clustering = tc.getMode() == Mode.CLUSTERING_AND_MICROAGGREGATION;
					MicroAggregationFunction func = tc.getMicroaggregationFunc()
							.createFunction(tc.isIgnoreMissingData());
					def.setMicroAggregationFunction(c.getName(), func, clustering);
				}
			}
		});
		ARXConfiguration arxConfig = ARXConfiguration.create();
		config.getPrivacyModels().forEach(m -> arxConfig.addPrivacyModel(m.createCriterion(defData, config)));
		config.getColumns().forEach(c -> arxConfig.setAttributeWeight(c.getName(), c.getWeight()));

		AnonymizationConfig aConfig = config.getAnonymizationConfig();
		if (aConfig.getHeuristicSearchEnabled().getBooleanValue()) {
			arxConfig.setHeuristicSearchEnabled(true);
			if (aConfig.getLimitSearchSteps().getBooleanValue()) {
				arxConfig.setHeuristicSearchStepLimit(aConfig.getSearchStepsLimit().getIntValue());
			}
			if (aConfig.getLimitSearchTime().getBooleanValue()) {
				arxConfig.setHeuristicSearchTimeLimit(aConfig.getSearchTimeLimit().getIntValue());
			}
		}

		arxConfig.setSuppressionLimit(aConfig.getSuppresionLimit().getDoubleValue());
		arxConfig.setPracticalMonotonicity(aConfig.getPractivalMonotonicity().getBooleanValue());
		arxConfig.setQualityModel(aConfig.getMeasure().createMetric());
		return arxConfig;
	}

	private HierarchyBuilder<?> getHierarchy(ColumnConfig c) {
		try {
			if (arxPortObject != null && arxPortObject.getHierarchies().containsKey(c.getName())) {
				return Utils.clone(arxPortObject.getHierarchies().get(c.getName()));
			}
			return HierarchyBuilder.create(c.getHierarchyFile());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public void clear() {

	}
}
