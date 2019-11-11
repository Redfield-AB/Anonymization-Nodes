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
package se.redfield.arxnode.hierarchy;

import java.io.IOException;
import java.util.ArrayList;
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

/**
 * Class for building hierarchy preview - a table with data anonymized using
 * different hierarchy levels.
 *
 */
public class HierarchyPreviewBuilder {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyPreviewBuilder.class);

	/**
	 * Build preview table.
	 * 
	 * @param inTable   Input data table.
	 * @param column    Column assigned to hierarchy.
	 * @param hierarchy Hierarchy builder instance.
	 * @param exec      Execution context.
	 * @return Buffered data table with hierarchy preview data.
	 * @throws IOException
	 */
	public BufferedDataTable build(BufferedDataTable inTable, String column, HierarchyBuilder<?> hierarchy,
			ExecutionContext exec) throws IOException {
		Map<String, HierarchyBuilder<?>> map = new HashMap<>();
		map.put(column, hierarchy);
		return build(inTable, map, exec);
	}

	/**
	 * Build preview table.
	 * 
	 * @param inTable     Input data table.
	 * @param hierarchies Hierarchies mapped by assigned column.
	 * @param exec        Execution context.
	 * @return Buffered data table with hierarchy preview data.
	 * @throws IOException
	 */
	public BufferedDataTable build(BufferedDataTable inTable, Map<String, HierarchyBuilder<?>> hierarchies,
			ExecutionContext exec) throws IOException {

		List<HierarchyPreview> previews = hierarchies
				.entrySet().stream().map(e -> new HierarchyPreview(e.getKey(),
						inTable.getDataTableSpec().findColumnIndex(e.getKey()), e.getValue()))
				.collect(Collectors.toList());

		for (DataRow row : inTable) {
			for (HierarchyPreview hp : previews) {
				DataCell cell = row.getCell(hp.getIndex());
				if (!cell.isMissing()) {
					hp.getData().add(Utils.toString(cell));
				}
			}
		}

		int levelsCount = previews.stream().map(HierarchyPreview::getColumnCount).max(Integer::compare).orElse(0);
		boolean multiplePreviews = previews.size() > 1;

		BufferedDataContainer container = exec.createDataContainer(createSpec(multiplePreviews, levelsCount));

		int columnCount = container.getTableSpec().getNumColumns();
		long rowIdx = 0;
		for (HierarchyPreview hp : previews) {
			String[][] preview = hp.getPreview();
			for (int i = 0; i < preview.length; i++) {
				String[] row = preview[i];
				List<DataCell> cells = new ArrayList<>();

				if (multiplePreviews) {
					cells.add(new StringCell(hp.getName()));
				}

				for (int j = 0; j < row.length; j++) {
					cells.add(new StringCell(row[j]));
				}

				container.addRowToTable(
						new DefaultRow(RowKey.createRowKey(rowIdx++), addMissingCells(columnCount, cells)));
			}
		}

		container.close();
		return container.getTable();
	}

	/**
	 * Create preview table spec.
	 * 
	 * @param hasAttribute Determines if an attribute column should be added. Only
	 *                     used in tables consists of multiple hierarchy previews.
	 * @param levelsCount  Max total hierarchy levels count.
	 * @return New spec.
	 */
	private DataTableSpec createSpec(boolean hasAttribute, int levelsCount) {
		List<DataColumnSpec> specss = new ArrayList<>();
		if (hasAttribute) {
			specss.add(new DataColumnSpecCreator("Attribute", StringCell.TYPE).createSpec());
		}
		for (int i = 0; i < levelsCount; i++) {
			specss.add(new DataColumnSpecCreator("Level-" + i, StringCell.TYPE).createSpec());
		}
		return new DataTableSpec(specss.toArray(new DataColumnSpec[] {}));
	}

	/**
	 * Add empty string cells to pad a row to match column count.
	 * 
	 * @param count Column count.
	 * @param cells List of row cells.
	 * @return List of row cells with empty cells added.
	 */
	private List<DataCell> addMissingCells(int count, List<DataCell> cells) {
		for (int i = cells.size(); i < count; i++) {
			cells.add(new StringCell(""));
		}
		return cells;
	}

	/**
	 * Data class to hold values for a single hierarchy preview.
	 *
	 */
	private class HierarchyPreview {
		private String name;
		private int index;
		private HierarchyBuilder<?> hierarchy;
		private Set<String> data;
		private String[][] preview;

		/**
		 * @param name      Associated column name.
		 * @param index     Associated column index.
		 * @param hierarchy Hierarchy builder instance
		 */
		public HierarchyPreview(String name, int index, HierarchyBuilder<?> hierarchy) {
			this.name = name;
			this.index = index;
			this.hierarchy = hierarchy;
			this.data = new HashSet<>();
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

		/**
		 * @return Hierarchy preview. A new instance created if necessary.
		 * @throws IOException
		 */
		public String[][] getPreview() throws IOException {
			if (preview == null) {
				preview = Utils.clone(hierarchy).build(data.toArray(new String[] {})).getHierarchy();
			}
			return preview;
		}

		/**
		 * @return Hierarchy preview table column count.
		 */
		public int getColumnCount() {
			try {
				return getPreview()[0].length;
			} catch (Exception e) {
				// ignore
			}
			return 0;
		}
	}
}
