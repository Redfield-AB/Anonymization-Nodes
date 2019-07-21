package se.redfield.arxnode.config.pmodels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import se.redfield.arxnode.config.SettingsModelConfig;
import se.redfield.arxnode.util.InterfaceAdapter;

public class PrivacyModelsConfig implements SettingsModelConfig {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(PrivacyModelsConfig.class);
	public static final String CONFIG_KEY = "privacyModels";
	public static final String CONFIG_JSON = "json";

	private List<AbstractPrivacyModelConfig> models = new ArrayList<>();

	public List<AbstractPrivacyModelConfig> getPrivacyModels() {
		return models;
	}

	@Override
	public Collection<? extends SettingsModelConfig> getChildred() {
		return models.stream().filter(m -> m.getIndex() > -1).collect(Collectors.toList());
	}

	public void save(NodeSettingsWO settings) {
		SettingsModelConfig.super.save(settings);
		String json = createGson().toJson(
				models.stream().filter(m -> m.getIndex() == -1).collect(Collectors.toList()).toArray(),
				AbstractPrivacyModelConfig[].class);
		settings.addString(CONFIG_JSON, json);
	}

	public void validate() throws InvalidSettingsException {
		if (models.size() == 0) {
			throw new InvalidSettingsException("No privacy models selected");
		}
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		models.clear();
		for (String key : settings.keySet()) {
			if (key.contains("-")) {
				AbstractPrivacyModelConfig model = AbstractPrivacyModelConfig.newInstance(key);
				if (model != null) {
					model.load(settings.getNodeSettings(key));
					models.add(model);
				}
			}
		}
		loadJson(settings);
	}

	private void loadJson(NodeSettingsRO settings) {
		String json = settings.getString(CONFIG_JSON, null);
		AbstractPrivacyModelConfig[] result = createGson().fromJson(json, AbstractPrivacyModelConfig[].class);
		if (result != null) {
			for (AbstractPrivacyModelConfig m : result) {
				models.add(m);
			}
		}
	}

	private static Gson createGson() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(AbstractPrivacyModelConfig.class,
				new InterfaceAdapter<AbstractPrivacyModelConfig>(KAnonymityConfig.class, DPresenceConfig.class,
						LDiversityConfig.class, TClosenessConfig.class, KMapConfig.class));
		return builder.create();
	}

	@Override
	public String getKey() {
		return CONFIG_KEY;
	}

}
