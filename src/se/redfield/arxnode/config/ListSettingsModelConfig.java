package se.redfield.arxnode.config;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

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
		for (String index : settings) {
			T child = createChild();
			child.load(settings.getNodeSettings(index));
			getChildred().add(child);
		}
	}

	@Override
	public void save(NodeSettingsWO settings) {
		int index = 0;
		for (SettingsModelConfig c : getChildred()) {
			NodeSettingsWO child = settings.addNodeSettings(String.valueOf(index++));
			c.save(child);
		}
	}

	protected abstract T createChild();
}
