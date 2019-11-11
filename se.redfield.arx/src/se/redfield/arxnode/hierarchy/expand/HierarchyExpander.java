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
package se.redfield.arxnode.hierarchy.expand;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.config.HierarchyBinding;
import se.redfield.arxnode.config.HierarchyExpandNodeConfig;

/**
 * Class for expanding a hierarchy to make in work with a different dataset.
 *
 * @param <T> {@link HierarchyBuilder} param
 * @param <H> {@link HierarchyBuilder} class
 */
public abstract class HierarchyExpander<T, H extends HierarchyBuilderGroupingBased<T>> {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyExpander.class);

	protected H src;
	protected int columnIndex;

	/**
	 * @param src         Source hierarchy builder.
	 * @param columnIndex Associated column index.
	 */
	protected HierarchyExpander(H src, int columnIndex) {
		this.src = src;
		this.columnIndex = columnIndex;
	}

	/**
	 * Processes the row. Checks if selected column cell is missing and has expected
	 * data type.
	 * 
	 * @param row Data table row.
	 */
	protected void processRow(DataRow row) {
		DataCell cell = row.getCell(columnIndex);
		if (!cell.isMissing()) {
			Class<? extends DataValue> expectedCellType = getExpectedCellType();
			if (!cell.getType().isCompatible(expectedCellType)) {
				throw new UnsupportedOperationException("Unsupported cell type: " + cell.getType().getName()
						+ ". Expected: " + expectedCellType.getSimpleName());
			}
			processCell(cell);
		}
	}

	/**
	 * Processes the cell value altering the hierarchy if necessary
	 * 
	 * @param cell
	 */
	protected abstract void processCell(DataCell cell);

	/**
	 * @return The expected cell type for a selected column.
	 */
	protected abstract Class<? extends DataValue> getExpectedCellType();

	/**
	 * Creates {@link HierarchyBuilder} instance.
	 * 
	 * @return Hierarchy builder.
	 */
	protected abstract HierarchyBuilderGroupingBased<T> createHierarchy();

	/**
	 * Creates Hierarchy expander instance based on type of provided hierarchy
	 * builder
	 * 
	 * @param src         Source hierarchy builder.
	 * @param columnIndex Column index associated with the hierarchy.
	 * @return Hierarchy expander.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static HierarchyExpander<?, ?> create(HierarchyBuilderGroupingBased<?> src, int columnIndex) {
		if (src instanceof HierarchyBuilderIntervalBased<?>) {
			DataType<?> type = src.getDataType();
			if (type instanceof ARXInteger) {
				return new HierarchyExpanderInterval.HierarchyExpanderArxInteger(
						(HierarchyBuilderIntervalBased<Long>) src, columnIndex);
			}
			if (type instanceof ARXDecimal) {
				return new HierarchyExpanderInterval.HierarchyExpanderArcDecimal(
						(HierarchyBuilderIntervalBased<Double>) src, columnIndex);
			}
			if (type instanceof ARXDate) {
				return new HierarchyExpanderInterval.HierarcyExpanderArxDate((HierarchyBuilderIntervalBased<Date>) src,
						columnIndex);
			}
			throw new UnsupportedOperationException(
					"Hierarchy datatype '" + type.getDescription().getLabel() + "' is not supported");
		} else {
			return new HierarchyExpanderOrder((HierarchyBuilderOrderBased<?>) src, columnIndex);
		}
	}

	/**
	 * Expands provided hierarchies based on a data from inTable adding any missing
	 * values and increasing hierarchy intervals if necessary. If the hierarchy
	 * doesn't require expanding it's left unchanged.
	 * 
	 * @param inTable             Input data table.
	 * @param config              Node config.
	 * @param existingHierarchies Hierarchies to expand. Mapped by the associated
	 *                            column names.
	 * @return Expanded hierarchies
	 * @throws IOException
	 */
	public static Map<String, HierarchyBuilder<?>> expand(BufferedDataTable inTable, HierarchyExpandNodeConfig config,
			Map<String, HierarchyBuilder<?>> existingHierarchies) throws IOException {
		Map<String, HierarchyExpander<?, ?>> expanders = new HashMap<>();
		Map<String, HierarchyBuilder<?>> result = new HashMap<>();
		for (HierarchyBinding b : config.getBindings()) {
			HierarchyBuilder<?> hb = getHierarchyBuilder(b, existingHierarchies);
			if (hb instanceof HierarchyBuilderGroupingBased) {
				expanders.put(b.getColumnName(), create((HierarchyBuilderGroupingBased<?>) hb,
						inTable.getDataTableSpec().findColumnIndex(b.getColumnName())));
			} else {
				logger.debug("Pass hierarchy unchanged for: " + b.getColumnName());
				result.put(b.getColumnName(), hb);
			}
		}
		for (DataRow row : inTable) {
			expanders.values().forEach(e -> e.processRow(row));
		}
		expanders.entrySet().forEach(e -> result.put(e.getKey(), e.getValue().createHierarchy()));
		return result;
	}

	/**
	 * Reads hierarchy builder from specified file or takes from existing
	 * hierarchies and returns a copy of it.
	 * 
	 * @param binding             Binding config for the hierarchy.
	 * @param existingHierarchies Existing hierarchies.
	 * @return Copy of a hierarchy builder.
	 * @throws IOException
	 */
	private static HierarchyBuilder<?> getHierarchyBuilder(HierarchyBinding binding,
			Map<String, HierarchyBuilder<?>> existingHierarchies) throws IOException {
		if (StringUtils.isNotEmpty(binding.getFileModel().getStringValue())) {
			return HierarchyBuilder.create(binding.getFile());
		}
		return Utils.clone(existingHierarchies.get(binding.getColumnName()));

	}
}
