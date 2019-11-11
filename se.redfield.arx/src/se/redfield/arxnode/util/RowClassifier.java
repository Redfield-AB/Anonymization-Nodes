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
package se.redfield.arxnode.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.deidentifier.arx.AttributeType;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;

/**
 * Class to distinguish anonymized rows based on theirs transformation
 * equivalence class.
 *
 */
public class RowClassifier {

	private List<Integer> indexes;
	private Map<Integer, Integer> classes;

	/**
	 * @param config Anonymizer node's config.
	 */
	public RowClassifier(Config config) {
		indexes = config.getColumns().stream().filter(c -> c.getAttrType() == AttributeType.QUASI_IDENTIFYING_ATTRIBUTE)
				.map(ColumnConfig::getIndex).collect(Collectors.toList());
		classes = new HashMap<>();
	}

	/**
	 * Computes hash for a given row.
	 * 
	 * @param row Row to compute a hash for.
	 * @return Hash value.
	 */
	private int hash(String[] row) {
		return indexes.stream().map(i -> row[i].hashCode()).reduce(0, (acc, hash) -> acc * 31 + hash).intValue();
	}

	/**
	 * Gets equivalence class for a given hash.
	 * 
	 * @param hash Row's hash value.
	 * @return Equivalence class.
	 */
	private int getClassForHash(int hash) {
		if (!classes.containsKey(hash)) {
			classes.put(hash, classes.size() + 1);
		}
		return classes.get(hash);
	}

	/**
	 * Computes equivalence class for a given row.
	 * 
	 * @param row Input row.
	 * @return Equivalence class.
	 */
	public int computeClass(String[] row) {
		return getClassForHash(hash(row));
	}
}
