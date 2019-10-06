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
package se.redfield.arxnode.nodes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;

public class ArxPortObjectSpec extends AbstractSimplePortObjectSpec {

	public static final String CONFIG_HIERARCHIES = "hierarchies";

	public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<ArxPortObjectSpec> {
	}

	private Set<String> hierarchies;

	public ArxPortObjectSpec() {
		this(null);
	}

	public ArxPortObjectSpec(ArxPortObjectSpec from) {
		hierarchies = new HashSet<>();
		if (from != null) {
			hierarchies.addAll(from.getHierarchies());
		}
	}

	@Override
	protected void save(ModelContentWO model) {
		model.addStringArray(CONFIG_HIERARCHIES, hierarchies.toArray(new String[] {}));
	}

	@Override
	protected void load(ModelContentRO model) throws InvalidSettingsException {
		hierarchies.clear();
		hierarchies.addAll(Arrays.asList(model.getStringArray(CONFIG_HIERARCHIES)));
	}

	public Set<String> getHierarchies() {
		return hierarchies;
	}

	@Override
	public String toString() {
		return "ArxPortObjectSpec: {hierarchies: [" + String.join(",", hierarchies) + "]}";
	}
}
