package se.redfield.arxnode.util;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.NodeLogger;

public interface TitledEnum {
	static final NodeLogger logger = NodeLogger.getLogger(TitledEnum.class);

	String getTitle();

	String name();

	static <E extends TitledEnum> E fromString(E[] values, String str, E def) {
		if (!StringUtils.isEmpty(str)) {
			for (E val : values) {
				if (str.equals(val.name()) || str.equals(val.getTitle())) {
					return val;
				}
			}
		}
		logger.warn(String.format("Value for '%s' not found. Using default: %s", str, def));
		return def;
	}

}
