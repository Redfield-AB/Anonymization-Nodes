package se.redfield.arxnode.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.deidentifier.arx.AttributeType;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;

public class RowClassifier {

	private List<Integer> indexes;
	private Map<Integer, Integer> classes;

	public RowClassifier(Config config) {
		indexes = config.getColumns().stream().filter(c -> c.getAttrType() == AttributeType.QUASI_IDENTIFYING_ATTRIBUTE)
				.map(ColumnConfig::getIndex).collect(Collectors.toList());
		classes = new HashMap<>();
	}

	private int hash(String[] row) {
		return indexes.stream().map(i -> row[i].hashCode()).reduce(0, (acc, hash) -> acc * 31 + hash).intValue();
	}

	private int getClassForHash(int hash) {
		if (!classes.containsKey(hash)) {
			classes.put(hash, classes.size() + 1);
		}
		return classes.get(hash);
	}

	public int computeClass(String[] row) {
		return getClassForHash(hash(row));
	}
}
