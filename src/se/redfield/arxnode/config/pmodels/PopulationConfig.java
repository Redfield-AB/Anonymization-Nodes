package se.redfield.arxnode.config.pmodels;

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXPopulationModel.Region;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.config.SettingsModelConfig;

public class PopulationConfig implements SettingsModelConfig {

	public static final String CONFIG_KEY = "population";
	public static final String CONFIG_REGION = "region";
	public static final String CONFIG_POPULATION_SIZE = "size";

	private String region;
	private long populationSize;

	public PopulationConfig() {
		region = Region.USA.name();
		populationSize = Region.USA.getPopulationSize();
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public long getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(long populationSize) {
		this.populationSize = populationSize;
	}

	public ARXPopulationModel getPopulationModel() {
		Region r = Utils.regionByName(region);
		if (r == Region.NONE) {
			return ARXPopulationModel.create(populationSize);
		}
		return ARXPopulationModel.create(r);
	}

	@Override
	public String getKey() {
		return CONFIG_KEY;
	}

	@Override
	public void save(NodeSettingsWO settings) {
		SettingsModelConfig.super.save(settings);
		settings.addString(CONFIG_REGION, region);
		settings.addLong(CONFIG_POPULATION_SIZE, populationSize);
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		SettingsModelConfig.super.load(settings);
		region = settings.getString(CONFIG_REGION);
		populationSize = settings.getLong(CONFIG_POPULATION_SIZE);
	}
}
