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

import org.apache.commons.lang.StringUtils;
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
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.Pair;

import se.redfield.arxnode.config.AnonymizationConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.config.TransformationConfig;
import se.redfield.arxnode.config.TransformationConfig.Mode;
import se.redfield.arxnode.partiton.PartitionInfo;
import se.redfield.arxnode.partiton.Partitioner;

public class Anonymizer {

	private static final NodeLogger logger = NodeLogger.getLogger(Anonymizer.class);

	private Config config;
	private ARXNode optimum;

	public Anonymizer(Config config) {
		this.config = config;
	}

	public BufferedDataTable[] process(BufferedDataTable inTable, ExecutionContext exec) throws Exception {
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
			service.submit(() -> {
				ARXAnonymizer anonymizer = new ARXAnonymizer();
				ARXResult result = anonymizer.anonymize(pair.getFirst(), configure(pair.getFirst()));
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
		BufferedDataTable anonTable = createDataTable(results, exec);
		return new BufferedDataTable[] { anonTable, createStatsTable(results, exec),
				createExceptionsTable(inTable, anonTable, exec) };
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
			for (int i = 0; i < outRow.getNumCells() && sameOrErased; i++) {
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
				totalSuppressedCount += suppresedRowsNum;

				DataCell[] cells = new DataCell[7];

				cells[0] = new DoubleCell(Double.valueOf(opt.getHighestScore().toString()));
				cells[1] = new StringCell(Arrays.toString(opt.getQuasiIdentifyingAttributes()));
				cells[2] = new StringCell(Arrays.toString(opt.getTransformation()));
				cells[3] = new StringCell(opt.getAnonymity().toString());
				cells[4] = new LongCell(pair.getSecond().getRows());
				cells[5] = new StringCell(pair.getSecond().getCriteria());
				cells[6] = new LongCell(suppresedRowsNum);

				RowKey key = new RowKey("Row" + row++);
				DataRow datarow = new DefaultRow(key, cells);
				container.addRowToTable(datarow);
			}
		}

		if (totalSuppressedCount > 0) {
			logger.warn("Some records were suppressed");
		}

		container.close();
		return container.getTable();
	}

	public DataTableSpec createStatsTableSpec() {
		DataColumnSpec[] outColSpecs = new DataColumnSpec[7];
		outColSpecs[0] = new DataColumnSpecCreator("Information Loss", DoubleCell.TYPE).createSpec();
		outColSpecs[1] = new DataColumnSpecCreator("Headers", StringCell.TYPE).createSpec();
		outColSpecs[2] = new DataColumnSpecCreator("Transformation", StringCell.TYPE).createSpec();
		outColSpecs[3] = new DataColumnSpecCreator("Anonymity", StringCell.TYPE).createSpec();
		outColSpecs[4] = new DataColumnSpecCreator("Row count", LongCell.TYPE).createSpec();
		outColSpecs[5] = new DataColumnSpecCreator("Partition criteria", StringCell.TYPE).createSpec();
		outColSpecs[6] = new DataColumnSpecCreator("Suppressed records", LongCell.TYPE).createSpec();
		return new DataTableSpec(outColSpecs);
	}

	private BufferedDataTable createDataTable(List<Pair<ARXResult, PartitionInfo>> results, ExecutionContext exec) {
		BufferedDataContainer container = exec.createDataContainer(createOutDataTableSpec());
		int rowIdx = 0;
		for (Pair<ARXResult, PartitionInfo> pair : results) {
			ARXResult res = pair.getFirst();
			if (res.isResultAvailable()) {
				Iterator<String[]> iter = res.getOutput(findOptimumNode(res)).iterator();
				iter.next();
				while (iter.hasNext()) {
					String[] row = iter.next();

					DataCell[] cells = new DataCell[row.length];
					for (int i = 0; i < cells.length; i++) {
						cells[i] = new StringCell(row[i]);
					}

					RowKey key = new RowKey("Row " + rowIdx++);
					DataRow datarow = new DefaultRow(key, cells);
					container.addRowToTable(datarow);
				}
			}
		}
		container.close();
		return container.getTable();
	}

	public DataTableSpec createOutDataTableSpec() {
		DataColumnSpec[] outColSpecs = new DataColumnSpec[config.getColumns().size()];
		config.getColumns().forEach(c -> {
			outColSpecs[c.getIndex()] = new DataColumnSpecCreator(c.getName(), StringCell.TYPE).createSpec();
		});
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
		return opt;
	}

	private ARXConfiguration configure(Data defData) {
		config.getColumns().forEach(c -> {
			DataDefinition def = defData.getDefinition();
			def.setAttributeType(c.getName(), c.getAttrType());

			if (c.getAttrType() == AttributeType.QUASI_IDENTIFYING_ATTRIBUTE) {
				HierarchyBuilder<?> hierarchy = getHierarchy(c.getHierarchyFile());
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

		boolean precomputationEnabled = aConfig.getPrecomputationEnabled().getBooleanValue();
		arxConfig.getQualityModel().getConfiguration().setPrecomputed(precomputationEnabled);
		if (precomputationEnabled) {
			arxConfig.getQualityModel().getConfiguration()
					.setPrecomputationThreshold(aConfig.getPrecomputationThreshold().getDoubleValue());
		}
		// logger.debug("ArxConfiguraton: \n" + Utils.toPrettyJson(arxConfig));
		// logger.debug("DataDefinition: \n" +
		// Utils.toPrettyJson(defData.getDefinition()));
		return arxConfig;
	}

	private HierarchyBuilder<?> getHierarchy(String path) {
		if (StringUtils.isEmpty(path)) {
			return null;
		}
		try {
			return HierarchyBuilder.create(path);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public void clear() {

	}
}
