package se.redfield.arxnode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;

import se.redfield.arxnode.config.AnonymizationConfig;
import se.redfield.arxnode.config.Config;

public class Anonymizer {

	private static final NodeLogger logger = NodeLogger.getLogger(Config.class);

	private Config config;
	private Map<String, HierarchyBuilder<?>> hierarchies = new HashMap<>();

	public Anonymizer(Config config) {
		this.config = config;
	}

	public BufferedDataTable process(BufferedDataTable inTable, ExecutionContext exec) {
		Utils.time();
		Data arxData = read(inTable);
		Utils.time("Read table");
		ARXConfiguration arxConfig = configure(arxData);
		Utils.time("Anon config");

		try {
			ARXAnonymizer anonymizer = new ARXAnonymizer();
			ARXResult res = anonymizer.anonymize(arxData, arxConfig);
			Utils.time("Anonymize");
			return write(res, exec);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private BufferedDataTable write(ARXResult res, ExecutionContext exec) {
		BufferedDataContainer container = exec.createDataContainer(this.config.createOutDataTableSpec());
		int rowIdx = 0;
		Iterator<String[]> iter = res.getOutput().iterator();
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
		container.close();
		return container.getTable();
	}

	private Data read(BufferedDataTable inTable) {
		DefaultData defData = Data.create();
		String[] columnNames = inTable.getDataTableSpec().getColumnNames();
		defData.add(columnNames);
		for (int i = 0; i < columnNames.length; i++) {
			defData.getDefinition().setDataType(columnNames[i], DataType.STRING);
		}

		inTable.forEach(row -> {
			defData.add(
					row.stream().map(cell -> cell.toString()).collect(Collectors.toList()).toArray(new String[] {}));
		});
		return defData;
	}

	private ARXConfiguration configure(Data defData) {
		config.getColumns().values().forEach(c -> {
			HierarchyBuilder<?> hierarchy = getHierarchy(c.getHierarchyFile());
			if (hierarchy != null) {
				defData.getDefinition().setAttributeType(c.getName(), hierarchy);
			} else {
				defData.getDefinition().setAttributeType(c.getName(), c.getAttrType());
			}
		});

		ARXConfiguration arxConfig = ARXConfiguration.create();
		config.getPrivacyModels().forEach(m -> arxConfig.addPrivacyModel(m.createCriterion()));
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
		logger.debug("ArxConfiguraton: \n" + Utils.toString(arxConfig));
		return arxConfig;
	}

	private HierarchyBuilder<?> getHierarchy(String path) {
		if (StringUtils.isEmpty(path)) {
			return null;
		}
		HierarchyBuilder<?> hierarchy = hierarchies.get(path);
		if (hierarchy == null) {
			try {
				hierarchy = HierarchyBuilder.create(path);
				hierarchies.put(path, hierarchy);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

		}
		return hierarchy;
	}

	public void clear() {
		hierarchies.clear();
	}
}
