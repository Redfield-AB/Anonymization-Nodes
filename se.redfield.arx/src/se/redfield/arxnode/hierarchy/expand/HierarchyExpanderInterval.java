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

import java.time.ZoneId;
import java.util.Date;

import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
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

/**
 * Hierarchy expander for interval based hierarchies.
 * 
 * @param <T>
 */
public abstract class HierarchyExpanderInterval<T> extends HierarchyExpander<T, HierarchyBuilderIntervalBased<T>> {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyExpanderInterval.class);

	protected T min;
	protected T max;

	protected HierarchyExpanderInterval(HierarchyBuilderIntervalBased<T> src, int columnIndex) {
		super(src, columnIndex);
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

	/**
	 * Creates new {@link Range} instance with a new minMax value.
	 * 
	 * @param from   Original range.
	 * @param minMax New minMax value.
	 * @return Adjusted range.
	 */
	protected Range<T> createRange(Range<T> from, T minMax) {
		return new Range<>(from.getSnapFrom(), from.getBottomTopCodingFrom(), minMax);
	}

	/**
	 * Hierarchy expander for {@link ARXInteger} interval hierarchies.
	 */
	static class HierarchyExpanderArxInteger extends HierarchyExpanderInterval<Long> {
		protected HierarchyExpanderArxInteger(HierarchyBuilderIntervalBased<Long> src, int columnIndex) {
			super(src, columnIndex);
		}

		@Override
		protected Class<? extends DataValue> getExpectedCellType() {
			return LongValue.class;
		}

		@Override
		protected void processCell(DataCell cell) {
			LongValue longCell = (LongValue) cell;
			long val = longCell.getLongValue();
			if (val <= min) {
				min = val - 1;
			}
			if (val >= max) {
				max = val + 1;
			}
		}
	}

	/**
	 * Hierarchy expander for {@link ARXDecimal} interval hierarchies.
	 *
	 */
	static class HierarchyExpanderArcDecimal extends HierarchyExpanderInterval<Double> {

		protected HierarchyExpanderArcDecimal(HierarchyBuilderIntervalBased<Double> src, int columnIndex) {
			super(src, columnIndex);
		}

		@Override
		protected Class<? extends DataValue> getExpectedCellType() {
			return DoubleValue.class;
		}

		@Override
		protected void processCell(DataCell cell) {
			DoubleValue doubleCell = (DoubleValue) cell;
			double val = doubleCell.getDoubleValue();
			if (val <= min) {
				min = val - 1;
			}
			if (val >= max) {
				max = val + 1;
			}
		}

	}

	/**
	 * Hierarchy expander for {@link ARXDate} interval hierarchies
	 *
	 */
	static class HierarcyExpanderArxDate extends HierarchyExpanderInterval<Date> {

		private static final int HOUR = 60 * 60 * 1000;

		protected HierarcyExpanderArxDate(HierarchyBuilderIntervalBased<Date> src, int columnIndex) {
			super(src, columnIndex);
		}

		@Override
		protected void processCell(DataCell cell) {
			LocalDateTimeValue dateCell = (LocalDateTimeValue) cell;
			long time = Date.from(dateCell.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()).getTime();
			if (time <= min.getTime()) {
				min = new Date(time - HOUR);
			}
			if (time >= max.getTime()) {
				max = new Date(time + HOUR);
			}
		}

		@Override
		protected Class<? extends DataValue> getExpectedCellType() {
			return LocalDateTimeValue.class;
		}

	}
}
