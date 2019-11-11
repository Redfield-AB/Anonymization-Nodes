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

import java.util.Comparator;
import java.util.Map;

import org.apache.commons.lang.reflect.FieldUtils;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataValue;
import org.knime.core.node.NodeLogger;

import se.redfield.arxnode.Utils;

/**
 * Hierarchy expander class for order based hierarchies.
 *
 * @param <T>
 */
public class HierarchyExpanderOrder<T> extends HierarchyExpander<T, HierarchyBuilderOrderBased<T>> {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyExpanderOrder.class);

	private Comparator<String> comparator;
	private Map<String, Integer> orderMap;
	private int nextOrder;

	@SuppressWarnings("unchecked")
	protected HierarchyExpanderOrder(HierarchyBuilderOrderBased<T> src, int columnIndex) {
		super(src, columnIndex);
		try {
			comparator = src.getComparator();
			orderMap = (Map<String, Integer>) FieldUtils.readDeclaredField(comparator, "val$map", true);
			nextOrder = orderMap.values().stream().max(Integer::compare).orElse(0) + 1;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	protected void processCell(DataCell cell) {
		if (orderMap != null) {
			String val = Utils.toString(cell);
			if (!orderMap.containsKey(val)) {
				logger.debug("new key: " + val);
				orderMap.put(val, nextOrder++);
			}
		}
	}

	@Override
	protected Class<? extends DataValue> getExpectedCellType() {
		return DataValue.class;
	}

	@Override
	protected HierarchyBuilderGroupingBased<T> createHierarchy() {
		return src;
	}

}
