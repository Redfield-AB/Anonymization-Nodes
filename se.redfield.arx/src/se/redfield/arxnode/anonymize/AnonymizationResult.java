package se.redfield.arxnode.anonymize;

import java.util.Arrays;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;

import se.redfield.arxnode.partiton.PartitionInfo;

public class AnonymizationResult {

	private ARXResult arxResult;
	private PartitionInfo partitionInfo;
	private int[] transformation;

	public AnonymizationResult(ARXResult arxResult, PartitionInfo partitionInfo) {
		this.arxResult = arxResult;
		this.partitionInfo = partitionInfo;
		if (arxResult.isResultAvailable()) {
			this.transformation = arxResult.getGlobalOptimum().getTransformation();
		}
	}

	public ARXResult getArxResult() {
		return arxResult;
	}

	public PartitionInfo getPartitionInfo() {
		return partitionInfo;
	}

	public int[] getTransformation() {
		if (transformation == null) {
			return arxResult.getGlobalOptimum().getTransformation();
		}
		return transformation;
	}

	public void setTransformation(int[] transformation) {
		this.transformation = transformation;
	}

	public ARXNode getCurrentNode() {
		return findNodeForTransfromation(transformation, true);
	}

	public ARXNode findNodeForTransfromation(int[] transform, boolean optimumIfNotFound) {
		if (transform != null) {
			for (ARXNode[] nodes : arxResult.getLattice().getLevels()) {
				for (ARXNode n : nodes) {
					if (Arrays.equals(transform, n.getTransformation())) {
						return n;
					}
				}
			}
		}
		return optimumIfNotFound ? arxResult.getGlobalOptimum() : null;
	}
}
