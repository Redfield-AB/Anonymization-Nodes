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
import org.deidentifier.arx.AttributeType;
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

import se.redfield.arxnode.config.Config;

public class Anonymizer {

	private static final NodeLogger logger = NodeLogger.getLogger(Config.class);

	private Config config;
	private Map<String, HierarchyBuilder<?>> hierarchies = new HashMap<>();

	public Anonymizer(Config config) {
		this.config = config;
	}

	public BufferedDataTable process(BufferedDataTable inTable, ExecutionContext exec) {
		BufferedDataTable outTable = null;
		Utils.time();
		DefaultData defData = Data.create();
		String[] columnNames = inTable.getDataTableSpec().getColumnNames();
		defData.add(columnNames);
		for (int i = 0; i < columnNames.length; i++) {
			// logger.warn("type:
			// "+inTable.getDataTableSpec().getColumnSpec(columnNames[i]).getType().getCellClass().getSimpleName());
			defData.getDefinition().setDataType(columnNames[i], DataType.STRING);
		}

		inTable.forEach(row -> {
			defData.add(
					row.stream().map(cell -> cell.toString()).collect(Collectors.toList()).toArray(new String[] {}));
		});
		Utils.time("Read table");

		config.getColumns().values().forEach(c -> {
			HierarchyBuilder<?> hierarchy = getHierarchy(c.getHierarchyFile());
			if (hierarchy != null) {
				defData.getDefinition().setAttributeType(c.getName(), hierarchy);
			} else if (c.getAttrType() != null) {
				defData.getDefinition().setAttributeType(c.getName(), c.getAttrType());
			} else {
				defData.getDefinition().setAttributeType(c.getName(), AttributeType.INSENSITIVE_ATTRIBUTE);
			}
		});

		ARXConfiguration arxConfig = ARXConfiguration.create();
		// config.addPrivacyModel(new KAnonymity(this.config.getKAnonymityFactor()));
		config.getPrivacyModels().forEach(m -> arxConfig.addPrivacyModel(m.createCriterion()));
		arxConfig.setSuppressionLimit(1.0);
		// config.setHeuristicSearchTimeLimit(100);
		Utils.time("Anon config");

		try {
			ARXAnonymizer anonymizer = new ARXAnonymizer();
			ARXResult res = anonymizer.anonymize(defData, arxConfig);
			Utils.time("Anonymize");

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
			outTable = container.getTable();
			Utils.time("write table");
			return outTable;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
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
