package se.redfield.arxnode.hierarchy.expand;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

import se.redfield.arxnode.config.HierarchyBinding;
import se.redfield.arxnode.config.HierarchyExpandNodeConfig;

public abstract class HierarchyExpander<T, HB extends HierarchyBuilderGroupingBased<T>> {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyExpander.class);

	protected HB src;
	protected int columnIndex;

	protected HierarchyExpander(HB src, int columnIndex) {
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

	@SuppressWarnings("unchecked")
	public static HierarchyExpander<?, ?> create(HierarchyBuilderGroupingBased<?> src, int columnIndex) {
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

	public static Map<String, HierarchyBuilder<?>> expand(BufferedDataTable inTable, HierarchyExpandNodeConfig config)
			throws IOException {
		Map<String, HierarchyExpander<?, ?>> expanders = new HashMap<>();
		for (HierarchyBinding b : config.getBindings()) {
			expanders.put(b.getColumnName(),
					create((HierarchyBuilderGroupingBased<?>) HierarchyBuilder.create(b.getFile()),
							inTable.getDataTableSpec().findColumnIndex(b.getColumnName())));
		}
		for (DataRow row : inTable) {
			expanders.values().forEach(e -> e.processRow(row));
		}
		Map<String, HierarchyBuilder<?>> result = new HashMap<>();
		expanders.entrySet().forEach(e -> result.put(e.getKey(), e.getValue().createHierarchy()));
		return result;
	}
}
