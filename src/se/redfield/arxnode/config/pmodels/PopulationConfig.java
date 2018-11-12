package se.redfield.arxnode.config.pmodels;

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXPopulationModel.Region;

import se.redfield.arxnode.Utils;

public class PopulationConfig {

	private String region;
	private long populationSize;

	public PopulationConfig() {
		region = Region.NONE.name();
		populationSize = 0;
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
}
