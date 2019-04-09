package se.redfield.arxnode.hierarchy.expand;

import java.time.ZoneOffset;
import java.util.Date;

import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased.Group;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased.Level;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Interval;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Range;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.LongValue;
import org.knime.core.data.time.localdatetime.LocalDateTimeValue;
import org.knime.core.node.NodeLogger;

public abstract class HierarchyExpanderInterval<T> extends HierarchyExpander<T, HierarchyBuilderIntervalBased<T>> {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyExpanderInterval.class);

	protected T min;
	protected T max;

	protected HierarchyExpanderInterval(HierarchyBuilderIntervalBased<T> src) {
		super(src);
		min = src.getLowerRange().getMinMaxValue();
		max = src.getUpperRange().getMinMaxValue();
	}

	@Override
	protected HierarchyBuilderGroupingBased<T> createHierarchy() {
		HierarchyBuilderIntervalBased<T> result = HierarchyBuilderIntervalBased.create(src.getDataType(),
				createRange(src.getLowerRange(), min), createRange(src.getUpperRange(), max));
		for (Interval<T> interval : src.getIntervals()) {
			result.addInterval(interval.getMin(), interval.getMax(), interval.getFunction());
		}

		for (Level<T> level : src.getLevels()) {
			Level<T> dstLvl = result.getLevel(level.getLevel());
			for (Group<T> group : level.getGroups()) {
				dstLvl.addGroup(group.getSize(), group.getFunction());
			}
		}
		result.setAggregateFunction(src.getDefaultFunction());
		return result;
	}

	protected Range<T> createRange(Range<T> from, T minMax) {
		return new Range<T>(from.getSnapFrom(), from.getBottomTopCodingFrom(), minMax);
	}

	static class HierarchyExpanderArxInteger extends HierarchyExpanderInterval<Long> {
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
				logger.debug("new min: " + val);
			}
			if (val >= max) {
				max = val + 1;
				logger.debug("new max: " + val);
			}
		}
	}

	static class HierarchyExpanderArcDecimal extends HierarchyExpanderInterval<Double> {

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
				logger.debug("new min: " + val);
			}
			if (val >= max) {
				max = val + 1;
				logger.debug("new max: " + val);
			}
		}

	}

	static class HierarcyExpanderArxDate extends HierarchyExpanderInterval<Date> {

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
				logger.debug("old max: " + max);
				max = new Date(time + HOUR);
				logger.debug("new max: " + max);
			}
		}

		@Override
		protected Class<? extends DataValue> getExpectedCellType() {
			return LocalDateTimeValue.class;
		}

	}
}
