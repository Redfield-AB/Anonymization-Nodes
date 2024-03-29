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
 *
 * This file incorporates work covered by the following copyright and  
 * permission notice:
 *
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.redfield.arxnode.hierarchy.edit;

import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Range;

/**
 * A model for interval-based builders.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyModelIntervals<T> extends HierarchyModelGrouping<T> {

	/**
	 * Constructor to create an initial definition.
	 *
	 * @param dataType
	 * @param data
	 */
	public HierarchyModelIntervals(final DataType<T> dataType, String[] data) {
		super(data, dataType, true);
		this.update();
	}

	@Override
	public HierarchyBuilderIntervalBased<T> getBuilder(boolean serializable) throws Exception {
		HierarchyBuilderIntervalBased<T> builder = HierarchyBuilderIntervalBased.create(super.getDataType(),
				new Range<T>(super.getLowerRange().snapBound, super.getLowerRange().bottomTopCodingBound,
						super.getLowerRange().minMaxBound),
				new Range<T>(super.getUpperRange().snapBound, super.getUpperRange().bottomTopCodingBound,
						super.getUpperRange().minMaxBound));

		builder.setAggregateFunction(this.getDefaultFunction());

		for (HierarchyWizardGroupingInterval<T> interval : super.getIntervals()) {
			builder.addInterval(interval.min, interval.max, interval.function);
		}

		int level = 0;
		for (List<HierarchyWizardGroupingGroup<T>> list : super.getModelGroups()) {
			for (HierarchyWizardGroupingGroup<T> group : list) {
				builder.getLevel(level).addGroup(group.size, group.function);
			}
			level++;
		}

		return builder;
	}

	@Override
	public void parse(HierarchyBuilder<T> builder) {

		if (!(builder instanceof HierarchyBuilderIntervalBased)) {
			return;
		}
		super.parse((HierarchyBuilderIntervalBased<T>) builder);
	}

	@Override
	protected void build() {
		super.hierarchy = null;
		super.error = null;
		super.groupsizes = null;

		if (data == null)
			return;

		HierarchyBuilderIntervalBased<T> builder = null;
		try {
			builder = getBuilder(false);
		} catch (Exception e) {
			super.error = e.getMessage();
			return;
		}

		String error = builder.isValid();
		if (error != null) {
			super.error = error;
			return;
		}

		try {
			super.groupsizes = builder.prepare(data);
		} catch (Exception e) {
			super.error = e.getMessage();
			return;
		}

		try {
			super.hierarchy = builder.build();
		} catch (Exception e) {
			super.error = e.getMessage();
		}
	}
}
