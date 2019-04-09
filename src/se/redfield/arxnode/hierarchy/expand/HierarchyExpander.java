package se.redfield.arxnode.hierarchy.expand;

import java.io.IOException;
import java.util.Date;

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

public abstract class HierarchyExpander<T, HB extends HierarchyBuilderGroupingBased<T>> {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyExpander.class);

	protected HB src;

	protected HierarchyExpander(HB src) {
		this.src = src;
	}

	public HierarchyBuilder<T> expand(BufferedDataTable inTable, int columnIndex) {
		for (DataRow row : inTable) {
			DataCell cell = row.getCell(columnIndex);
			if (!cell.isMissing()) {
				Class<? extends DataValue> expectedCellType = getExpectedCellType();
				if (!cell.getType().isCompatible(expectedCellType)) {
					throw new UnsupportedOperationException("Unsupported cell type: " + cell.getType().getName()
							+ ". Expected: " + expectedCellType.getSimpleName());
				}
				testRow(cell);
			}
		}

		return createHierarchy();
	}

	protected abstract void testRow(DataCell cell);

	protected abstract Class<? extends DataValue> getExpectedCellType();

	protected abstract HierarchyBuilderGroupingBased<T> createHierarchy();

	@SuppressWarnings("unchecked")
	public static HierarchyExpander<?, ?> create(HierarchyBuilderGroupingBased<?> src) {
		if (src instanceof HierarchyBuilderIntervalBased<?>) {
			DataType<?> type = src.getDataType();
			if (type instanceof ARXInteger) {
				return new HierarchyExpanderInterval.HierarchyExpanderArxInteger(
						(HierarchyBuilderIntervalBased<Long>) src);
			}
			if (type instanceof ARXDecimal) {
				return new HierarchyExpanderInterval.HierarchyExpanderArcDecimal(
						(HierarchyBuilderIntervalBased<Double>) src);
			}
			if (type instanceof ARXDate) {
				return new HierarchyExpanderInterval.HierarcyExpanderArxDate((HierarchyBuilderIntervalBased<Date>) src);
			}
			throw new UnsupportedOperationException(
					"Hierarchy datatype '" + type.getDescription().getLabel() + "' is not supported");
		} else {
			return new HierarchyExpanderOrder((HierarchyBuilderOrderBased<?>) src);
		}
	}

	public static HierarchyBuilder<?> expand(BufferedDataTable inTable, String hierarchyFile, int columnIndex)
			throws IOException {
		HierarchyExpander<?, ?> expander = create(
				(HierarchyBuilderGroupingBased<?>) HierarchyBuilder.create(hierarchyFile));
		return expander.expand(inTable, columnIndex);
	}
}
