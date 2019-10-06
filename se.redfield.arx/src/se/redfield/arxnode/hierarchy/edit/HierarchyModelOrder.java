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

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;

/**
 * A model for order-based builders.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyModelOrder<T> extends HierarchyModelGrouping<T> {

	/** Locale. */
	private Locale locale;

	/**
	 * Constructor to create an initial definition.
	 *
	 * @param dataType
	 * @param locale
	 * @param data
	 */
	public HierarchyModelOrder(final DataType<T> dataType, final Locale locale, String[] data) {
		super(data, dataType, false);
		this.locale = locale;
		this.update();
	}

	@Override
	public HierarchyBuilderOrderBased<T> getBuilder(boolean serializable) throws Exception {

		HierarchyBuilderOrderBased<T> builder;
		if (serializable) {
			builder = HierarchyBuilderOrderBased.create(super.getDataType(), data);
		} else {
			builder = HierarchyBuilderOrderBased.create(super.getDataType(), false);
		}

		builder.setAggregateFunction(this.getDefaultFunction());
		int level = 0;
		for (List<HierarchyWizardGroupingGroup<T>> list : super.getModelGroups()) {
			for (HierarchyWizardGroupingGroup<T> group : list) {
				builder.getLevel(level).addGroup(group.size, group.function);
			}
			level++;
		}

		return builder;
	}

	/**
	 * Returns the current locale.
	 *
	 * @return
	 */
	public Locale getLocale() {
		if (locale == null) {
			return Locale.getDefault();
		} else {
			return locale;
		}
	}

	/**
	 * Moves an item down.
	 *
	 * @param index
	 * @return
	 */
	public boolean moveDown(int index) {
		if (index >= data.length - 1 || index < 0)
			return false;
		String temp = data[index + 1];
		data[index + 1] = data[index];
		data[index] = temp;
		update();
		return true;
	}

	/**
	 * Moves an item up.
	 *
	 * @param index
	 * @return
	 */
	public boolean moveUp(int index) {
		if (index <= 0)
			return false;
		String temp = data[index - 1];
		data[index - 1] = data[index];
		data[index] = temp;
		update();
		return true;
	}

	/**
	 * Moves an item
	 * 
	 * @param from initial index
	 * @param to   destination index
	 * @return
	 */
	public boolean move(int from, int to) {
		if (from < 0 || from >= data.length || to < 0 || to >= data.length) {
			return false;
		}
		if (from == to) {
			return true;
		}

		String elem = data[from];
		if (from < to) {
			for (int i = from; i < to; i++) {
				data[i] = data[i + 1];
			}
		} else {
			for (int i = from; i > to; i--) {
				data[i] = data[i - 1];
			}
		}
		data[to] = elem;
		update();

		return true;
	}

	@Override
	public void parse(HierarchyBuilder<T> builder) {

		if (!(builder instanceof HierarchyBuilderOrderBased)) {
			return;
		}
		super.parse((HierarchyBuilderOrderBased<T>) builder);
	}

	/**
	 * Sorts the data.
	 *
	 * @param type
	 * @return
	 */
	public boolean sort(final DataType<?> type) {

		boolean result = true;
		try {
			Arrays.sort(this.data, (o1, o2) -> {
				try {
					return type.compare(o1, o2);
				} catch (NumberFormatException | ParseException e) {
					throw new RuntimeException(e);
				}
			});
		} catch (Exception e) {
			result = false;
		}
		update();
		return result;
	}

	@Override
	protected void build() {
		super.hierarchy = null;
		super.error = null;
		super.groupsizes = null;

		if (data == null)
			return;

		HierarchyBuilderOrderBased<T> builder = null;
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
