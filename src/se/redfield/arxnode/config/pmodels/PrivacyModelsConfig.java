package se.redfield.arxnode.config.pmodels;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.util.InterfaceAdapter;

public class PrivacyModelsConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(PrivacyModelsConfig.class);

	private List<PrivacyModelConfig> models = new ArrayList<>();

	public List<PrivacyModelConfig> getModels() {
		return models;
	}

	public void save(NodeSettingsWO settings) {
		String json = createGson().toJson(this);
		settings.addString(Config.CONFIG_PRIVACY_MODELS, json);
	}

	public void validate() throws InvalidSettingsException {
		if (models.size() == 0) {
			throw new InvalidSettingsException("No privacy models selected");
		}
	}

	public static PrivacyModelsConfig load(NodeSettingsRO settings) {
		PrivacyModelsConfig result = null;
		try {
			String json = settings.getString(Config.CONFIG_PRIVACY_MODELS, null);

			result = createGson().fromJson(json, PrivacyModelsConfig.class);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		if (result == null) {
			result = new PrivacyModelsConfig();
		}
		return result;
	}

	private static Gson createGson() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(PrivacyModelConfig.class, new InterfaceAdapter<PrivacyModelConfig>(
				KAnonymityConfig.class, DPresenceConfig.class, LDiversityConfig.class));
		return builder.create();
	}

}
