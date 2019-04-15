package se.redfield.arxnode.config;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

public abstract class ListSettingsModelConfig<T extends SettingsModelConfig> implements SettingsModelConfig {

	protected List<T> children;

	@Override
	public List<T> getChildred() {
		if (children == null) {
			children = new ArrayList<>();
		}
		return children;
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		getChildred().clear();
		for (String key : settings) {
			T child = createChild();
			System.out.println(key);
			child.load(settings.getNodeSettings(key));
			getChildred().add(child);
		}
	}

	protected abstract T createChild();
}
