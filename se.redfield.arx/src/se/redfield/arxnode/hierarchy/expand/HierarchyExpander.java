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

public abstract class HierarchyExpander<T, H extends HierarchyBuilderGroupingBased<T>> {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyExpander.class);

	protected H src;
	protected int columnIndex;

	protected HierarchyExpander(H src, int columnIndex) {
		this.src = src;
		this.columnIndex = columnIndex;
	}

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

	protected abstract void processCell(DataCell cell);

	protected abstract Class<? extends DataValue> getExpectedCellType();

	protected abstract HierarchyBuilderGroupingBased<T> createHierarchy();

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

	private static HierarchyBuilder<?> getHierarchyBuilder(HierarchyBinding binding,
			Map<String, HierarchyBuilder<?>> existingHierarchies) throws IOException {
		if (StringUtils.isNotEmpty(binding.getFileModel().getStringValue())) {
			return HierarchyBuilder.create(binding.getFile());
		}
		return Utils.clone(existingHierarchies.get(binding.getColumnName()));

	}
}
