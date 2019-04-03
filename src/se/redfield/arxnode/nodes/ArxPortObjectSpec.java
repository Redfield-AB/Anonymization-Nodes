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

	public ArxPortObjectSpec clone() {
		return new ArxPortObjectSpec(this);
	}

	@Override
	public String toString() {
		return "ArxPortObjectSpec: {hierarchies: [" + String.join(",", hierarchies) + "]}";
	}
}
