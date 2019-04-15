package se.redfield.arxnode.hierarchy.expand;

import java.util.Comparator;
import java.util.Map;

import org.apache.commons.lang.reflect.FieldUtils;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataValue;
import org.knime.core.node.NodeLogger;

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
			String val = cell.toString();
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
