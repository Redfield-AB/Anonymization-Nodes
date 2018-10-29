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
import java.util.stream.Collectors;

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
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;

import se.redfield.arxnode.config.AnonymizationConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.config.TransformationConfig;
import se.redfield.arxnode.config.TransformationConfig.Mode;

public class Anonymizer {

	private static final NodeLogger logger = NodeLogger.getLogger(Anonymizer.class);

	private Config config;
	private ARXNode optimum;

	public Anonymizer(Config config) {
		this.config = config;
	}

	public BufferedDataTable[] process(BufferedDataTable inTable, ExecutionContext exec, int threadsNum)
			throws Exception {
		List<Data> arxData = read(inTable, threadsNum);

		ExecutorService executor = Executors.newFixedThreadPool(arxData.size());
		CompletionService<ARXResult> service = new ExecutorCompletionService<>(executor);
		for (Data data : arxData) {
			service.submit(() -> {
				ARXAnonymizer anonymizer = new ARXAnonymizer();
				logger.info("data:" + data.getHandle().getNumRows());
				return anonymizer.anonymize(data, configure(data));
			});
		}
		int received = 0;
		List<ARXResult> results = new ArrayList<>();
		while (received++ < arxData.size()) {
			try {
				logger.info("take");
				results.add(service.take().get());
			} catch (InterruptedException | ExecutionException e) {
				executor.shutdownNow();
				throw e;
			}
		}

		optimum = findSingleOptimum(results);
		return new BufferedDataTable[] { createDataTable(results, exec), createStatsTable(results, exec) };
	}

	private ARXNode findSingleOptimum(List<ARXResult> results) {
		if (config.getAnonymizationConfig().getPartitionsSingleOptimum().getBooleanValue()) {
			return results.stream().filter(ARXResult::isResultAvailable).map(ARXResult::getGlobalOptimum).findFirst()
					.get();
		}
		return null;
	}

	private BufferedDataTable createStatsTable(List<ARXResult> results, ExecutionContext exec) {
		BufferedDataContainer container = exec.createDataContainer(createStatsTableSpec());
		int row = 0;
		for (ARXResult res : results) {
			ARXNode opt = findOptimumNode(res);
			DataCell[] cells = new DataCell[4];

			cells[0] = new DoubleCell(Double.valueOf(opt.getHighestScore().toString()));
			cells[1] = new StringCell(Arrays.toString(opt.getQuasiIdentifyingAttributes()));
			cells[2] = new StringCell(Arrays.toString(opt.getTransformation()));
			cells[3] = new StringCell(opt.getAnonymity().toString());

			RowKey key = new RowKey("Row" + row++);
			DataRow datarow = new DefaultRow(key, cells);
			container.addRowToTable(datarow);
		}

		container.close();
		return container.getTable();
	}

	public DataTableSpec createStatsTableSpec() {
		DataColumnSpec[] outColSpecs = new DataColumnSpec[4];
		outColSpecs[0] = new DataColumnSpecCreator("Information Loss", DoubleCell.TYPE).createSpec();
		outColSpecs[1] = new DataColumnSpecCreator("Headers", StringCell.TYPE).createSpec();
		outColSpecs[2] = new DataColumnSpecCreator("Transformation", StringCell.TYPE).createSpec();
		outColSpecs[3] = new DataColumnSpecCreator("Anonymity", StringCell.TYPE).createSpec();
		return new DataTableSpec(outColSpecs);
	}

	private BufferedDataTable createDataTable(List<ARXResult> results, ExecutionContext exec) {
		BufferedDataContainer container = exec.createDataContainer(this.config.createOutDataTableSpec());
		int rowIdx = 0;
		for (ARXResult res : results) {
			if (res.isResultAvailable()) {
				Iterator<String[]> iter = res.getOutput(findOptimumNode(res)).iterator();
				iter.next();
				while (iter.hasNext()) {
					String[] row = iter.next();
					// logger.warn("row" + Arrays.toString(row));

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

	private List<Data> read(BufferedDataTable inTable, int parts) {
		List<Data> result = new ArrayList<>();
		long partitionSize = inTable.size() / parts;
		long index = 0;
		DefaultData current = createData(inTable);
		result.add(current);
		for (DataRow row : inTable) {
			current.add(
					row.stream().map(cell -> cell.toString()).collect(Collectors.toList()).toArray(new String[] {}));
			if (++index >= partitionSize && result.size() < parts) {
				current = createData(inTable);
				result.add(current);
				index = 0;
			}
		}
		return result;
	}

	private DefaultData createData(BufferedDataTable inTable) {
		DefaultData defData = Data.create();
		String[] columnNames = inTable.getDataTableSpec().getColumnNames();
		defData.add(columnNames);
		for (int i = 0; i < columnNames.length; i++) {
			DataType type = inTable.getDataTableSpec().getColumnSpec(columnNames[i]).getType();
			defData.getDefinition().setDataType(columnNames[i], Utils.knimeToArxType(type));
		}

		return defData;
	}

	private ARXConfiguration configure(Data defData) {
		config.getColumns().values().forEach(c -> {
			DataDefinition def = defData.getDefinition();
			HierarchyBuilder<?> hierarchy = getHierarchy(c.getHierarchyFile());
			if (hierarchy != null) {
				def.setAttributeType(c.getName(), hierarchy);
			} else {
				def.setAttributeType(c.getName(), c.getAttrType());
			}

			if (c.getAttrType() == AttributeType.QUASI_IDENTIFYING_ATTRIBUTE) {
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
		config.getPrivacyModels().forEach(m -> arxConfig.addPrivacyModel(m.createCriterion(defData)));
		config.getColumns().values().forEach(c -> arxConfig.setAttributeWeight(c.getName(), c.getWeight()));

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
