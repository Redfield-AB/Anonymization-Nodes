package se.redfield.arxnode.hierarchy;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;

import se.redfield.arxnode.Utils;

public class HierarchyPreviewBuilder {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyPreviewBuilder.class);

	public BufferedDataTable build(BufferedDataTable inTable, String column, HierarchyBuilder<?> hierarchy,
			ExecutionContext exec) throws IOException {
		Map<String, HierarchyBuilder<?>> map = new HashMap<>();
		map.put(column, hierarchy);
		return build(inTable, map, exec);
	}

	public BufferedDataTable build(BufferedDataTable inTable, Map<String, HierarchyBuilder<?>> hierarchies,
			ExecutionContext exec) throws IOException {

		List<HierarchyPreview> previews = hierarchies
				.entrySet().stream().map(e -> new HierarchyPreview(e.getKey(),
						inTable.getDataTableSpec().findColumnIndex(e.getKey()), e.getValue()))
				.collect(Collectors.toList());

		for (DataRow row : inTable) {
			for (HierarchyPreview hp : previews) {
				hp.getData().add(Utils.toString(row.getCell(hp.getIndex())));
			}
		}

		int columnCount = previews.stream().map(hp -> hp.getColumnCount()).max(Integer::compare).orElse(0);

		DataColumnSpec[] specs = new DataColumnSpec[columnCount];
		for (int i = 0; i < specs.length; i++) {
			specs[i] = new DataColumnSpecCreator("Level-" + i, StringCell.TYPE).createSpec();
		}
		BufferedDataContainer container = exec.createDataContainer(new DataTableSpec(specs));

		long rowIdx = 0;
		for (HierarchyPreview hp : previews) {
			StringCell headerCell = new StringCell(hp.getName());
			container.addRowToTable(
					new DefaultRow(RowKey.createRowKey(rowIdx++), addMissingCells(columnCount, headerCell)));

			String[][] preview = hp.getPreview();
			for (int i = 0; i < preview.length; i++) {
				String[] row = preview[i];
				DataCell[] cells = new DataCell[row.length];

				for (int j = 0; j < row.length; j++) {
					cells[j] = new StringCell(row[j]);
				}

				container.addRowToTable(
						new DefaultRow(RowKey.createRowKey(rowIdx++), addMissingCells(columnCount, cells)));
			}
		}

		container.close();
		return container.getTable();
	}

	private DataCell[] addMissingCells(int count, DataCell... cells) {
		DataCell[] result = new DataCell[count];
		for (int i = 0; i < result.length; i++) {
			if (i < cells.length) {
				result[i] = cells[i];
			} else {
				result[i] = new StringCell("");
			}
		}
		return result;
	}

	private class HierarchyPreview {
		private String name;
		private int index;
		private HierarchyBuilder<?> hierarchy;
		private Set<String> data;
		private String[][] preview;

		public HierarchyPreview(String name, int index, HierarchyBuilder<?> hierarchy) {
			this.name = name;
			this.index = index;
			this.hierarchy = hierarchy;
			this.data = new HashSet<String>();
		}

		public String getName() {
			return name;
		}

		public int getIndex() {
			return index;
		}

		public Set<String> getData() {
			return data;
		}

		public String[][] getPreview() throws IOException {
			if (preview == null) {
				preview = Utils.clone(hierarchy).build(data.toArray(new String[] {})).getHierarchy();
			}
			return preview;
		}

		public int getColumnCount() {
			try {
				return getPreview()[0].length;
			} catch (Exception e) {
			}
			return 0;
		}
	}
}
