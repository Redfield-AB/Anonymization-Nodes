package se.redfield.arxnode.anonymize;

import org.deidentifier.arx.ARXResult;

import se.redfield.arxnode.partiton.PartitionInfo;

public class AnonymizationResult {

	private ARXResult arxResult;
	private PartitionInfo partitionInfo;

	public AnonymizationResult(ARXResult arxResult, PartitionInfo partitionInfo) {
		this.arxResult = arxResult;
		this.partitionInfo = partitionInfo;
	}

	public ARXResult getArxResult() {
		return arxResult;
	}

	public PartitionInfo getPartitionInfo() {
		return partitionInfo;
	}
}
