package se.redfield.arxnode.util;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Date;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased.Group;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased.Level;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Interval;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Range;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.LongValue;
import org.knime.core.data.time.localdatetime.LocalDateTimeValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;

public abstract class HierarchyExpander<T> {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyExpander.class);

	protected HierarchyBuilderIntervalBased<T> src;
	protected T min;
	protected T max;

	protected HierarchyExpander(HierarchyBuilderIntervalBased<T> src) {
		this.src = src;
		min = src.getLowerRange().getMinMaxValue();
		max = src.getUpperRange().getMinMaxValue();
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

		HierarchyBuilderIntervalBased<T> result = HierarchyBuilderIntervalBased.create(src.getDataType(),
				createRange(src.getLowerRange(), min), createRange(src.getUpperRange(), max));
		logger.warn("result: " + result.getLowerRange() + "\n" + result.getUpperRange());
		for (Interval<T> interval : src.getIntervals()) {
			logger.warn(interval);
			result.addInterval(interval.getMin(), interval.getMax(), interval.getFunction());
		}
		for (Level<T> level : src.getLevels()) {
			logger.warn(level);
			Level<T> dstLvl = result.getLevel(level.getLevel());
			for (Group<T> group : level.getGroups()) {
				logger.warn(group);
				dstLvl.addGroup(group.getSize(), group.getFunction());
			}
		}
		result.setAggregateFunction(src.getDefaultFunction());
		return result;
	}

	protected Range<T> createRange(Range<T> from, T minMax) {
		return new Range<T>(from.getSnapFrom(), from.getBottomTopCodingFrom(), minMax);
	}

	protected abstract void testRow(DataCell cell);

	protected abstract Class<? extends DataValue> getExpectedCellType();

	public static HierarchyBuilder<?> expand(BufferedDataTable inTable, String hierarchyFile, int columnIndex)
			throws IOException {
		HierarchyExpander<?> expander = create(
				(HierarchyBuilderIntervalBased<?>) HierarchyBuilder.create(hierarchyFile));
		return expander.expand(inTable, columnIndex);
	}

	@SuppressWarnings("unchecked")
	public static HierarchyExpander<?> create(HierarchyBuilderIntervalBased<?> src) {
		DataType<?> type = src.getDataType();
		if (type instanceof ARXInteger) {
			return new HierarchyExpanderArxInteger((HierarchyBuilderIntervalBased<Long>) src);
		}
		if (type instanceof ARXDecimal) {
			return new HierarchyExpanderArcDecimal((HierarchyBuilderIntervalBased<Double>) src);
		}
		if (type instanceof ARXDate) {
			return new HierarcyExpanderArxDate((HierarchyBuilderIntervalBased<Date>) src);
		}
		throw new UnsupportedOperationException(
				"Hierarchy datatype '" + type.getDescription().getLabel() + "' is not supported");
	}

	private static class HierarchyExpanderArxInteger extends HierarchyExpander<Long> {
		protected HierarchyExpanderArxInteger(HierarchyBuilderIntervalBased<Long> src) {
			super(src);
		}

		@Override
		protected Class<? extends DataValue> getExpectedCellType() {
			return LongValue.class;
		}

		@Override
		protected void testRow(DataCell cell) {
			LongValue longCell = (LongValue) cell;
			long val = longCell.getLongValue();
			if (val <= min) {
				min = val - 1;
				logger.warn("new min: " + val);
			}
			if (val >= max) {
				max = val + 1;
				logger.warn("new max: " + val);
			}
		}
	}

	private static class HierarchyExpanderArcDecimal extends HierarchyExpander<Double> {

		protected HierarchyExpanderArcDecimal(HierarchyBuilderIntervalBased<Double> src) {
			super(src);
		}

		@Override
		protected Class<? extends DataValue> getExpectedCellType() {
			return DoubleValue.class;
		}

		@Override
		protected void testRow(DataCell cell) {
			DoubleValue doubleCell = (DoubleValue) cell;
			double val = doubleCell.getDoubleValue();
			if (val <= min) {
				min = val - 1;
				logger.warn("new min: " + val);
			}
			if (val >= max) {
				max = val + 1;
				logger.warn("new max: " + val);
			}
		}

	}

	private static class HierarcyExpanderArxDate extends HierarchyExpander<Date> {

		private static long HOUR = 60 * 60 * 1000;

		protected HierarcyExpanderArxDate(HierarchyBuilderIntervalBased<Date> src) {
			super(src);
		}

		@Override
		protected void testRow(DataCell cell) {
			LocalDateTimeValue dateCell = (LocalDateTimeValue) cell;
			long time = Date.from(dateCell.getLocalDateTime().atZone(ZoneOffset.systemDefault()).toInstant()).getTime();
			if (time <= min.getTime()) {
				min = new Date(time - HOUR);
			}
			if (time >= max.getTime()) {
				logger.warn("old max: " + max);
				max = new Date(time + HOUR);
				logger.warn("new max: " + max);
			}
		}

		@Override
		protected Class<? extends DataValue> getExpectedCellType() {
			return LocalDateTimeValue.class;
		}

//		@Override
//		protected Range<Date> createRange(Range<Date> from, Date minMax) {
//			return new Range<Date>(minMax, minMax, minMax);
//		}
	}
}
