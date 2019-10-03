package se.redfield.arxnode.partiton;

import org.deidentifier.arx.Data.DefaultData;

public class Partition {

	private DefaultData data;
	private PartitionInfo info;

	public Partition(DefaultData data) {
		this.data = data;
		this.info = new PartitionInfo();
	}

	public DefaultData getData() {
		return data;
	}

	public PartitionInfo getInfo() {
		return info;
	}
}
