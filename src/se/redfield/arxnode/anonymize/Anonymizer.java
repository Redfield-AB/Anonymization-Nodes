package se.redfield.arxnode.anonymize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.MicroAggregationFunction;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.config.AnonymizationConfig;
import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.config.TransformationConfig;
import se.redfield.arxnode.config.TransformationConfig.Mode;
import se.redfield.arxnode.nodes.ArxPortObject;
import se.redfield.arxnode.partiton.Partition;
import se.redfield.arxnode.partiton.Partitioner;

public class Anonymizer {
	private static final NodeLogger logger = NodeLogger.getLogger(Anonymizer.class);

	public static final String ROW_KEY = "RowKey";

	private Config config;
	private ArxPortObject arxPortObject;

	public Anonymizer(Config config) {
		this.config = config;
	}

	public List<AnonymizationResult> process(BufferedDataTable inTable, ArxPortObject arxObject, ExecutionContext exec)
			throws Exception {
		this.arxPortObject = arxObject;
		AnonymizationConfig anonConfig = config.getAnonymizationConfig();
		Partitioner partitioner = Partitioner.createPartitioner(anonConfig.getNumOfThreads().getIntValue(),
				anonConfig.getPartitionsGroupByEnabled().getBooleanValue()
						? anonConfig.getPartitionsGroupByColumn().getStringValue()
						: "",
				inTable);
		List<Partition> parts = partitioner.partition(inTable,
				config.getAnonymizationConfig().getOmitMissingValues().getBooleanValue());

		ExecutorService executor = Executors.newFixedThreadPool(parts.size());
		CompletionService<AnonymizationResult> service = new ExecutorCompletionService<>(executor);
		for (Partition pair : parts) {
			ARXConfiguration arxConfig = configure(pair.getData());
			service.submit(() -> {
				ARXAnonymizer anonymizer = new ARXAnonymizer();
				ARXResult result = anonymizer.anonymize(pair.getData(), arxConfig);
				return new AnonymizationResult(result, pair.getInfo());
			});
		}
		int received = 0;
		List<AnonymizationResult> results = new ArrayList<>();
		while (received++ < parts.size()) {
			try {
				results.add(service.take().get());
			} catch (InterruptedException | ExecutionException e) {
				executor.shutdownNow();
				throw e;
			}
		}
		executor.shutdown();

		return results;
	}

	private ARXConfiguration configure(Data defData) {
		config.getColumns().forEach(c -> {
			DataDefinition def = defData.getDefinition();
			def.setAttributeType(c.getName(), c.getAttrType());

			if (c.getAttrType() == AttributeType.QUASI_IDENTIFYING_ATTRIBUTE) {
				HierarchyBuilder<?> hierarchy = getHierarchy(c);
				if (hierarchy != null) {
					def.setAttributeType(c.getName(), hierarchy);
				}

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
			File hierarchyFile = c.getHierarchyFile();
			if (hierarchyFile == null) {
				return null;
			}
			return HierarchyBuilder.create(hierarchyFile);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
}
