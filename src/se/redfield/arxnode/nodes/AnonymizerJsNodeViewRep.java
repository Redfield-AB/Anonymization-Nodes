package se.redfield.arxnode.nodes;

import java.util.List;

import org.apache.mahout.math.Arrays;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;

import se.redfield.arxnode.anonymize.AnonymizationResult;

public class AnonymizerJsNodeViewRep extends JSONViewContent {

	private AnonymizationResultRep[] partitions;

	public void updateFrom(List<AnonymizationResult> results) {
		partitions = new AnonymizationResultRep[results.size()];
		for (int i = 0; i < partitions.length; i++) {
			partitions[i] = new AnonymizationResultRep(results.get(i));
		}
	}

	public AnonymizationResultRep[] getPartitions() {
		return partitions;
	}

	public void setPartitions(AnonymizationResultRep[] partitions) {
		this.partitions = partitions;
	}

	@Override
	public void saveToNodeSettings(NodeSettingsWO settings) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AnonymizerJsNodeViewRep) {
			AnonymizerJsNodeViewRep other = (AnonymizerJsNodeViewRep) obj;
			if (partitions == null) {
				return other.partitions == null;
			}
			return partitions.equals(other.partitions);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return partitions == null ? 0 : partitions.hashCode();
	}

	public static class AnonymizationResultRep {

		private ArxNodeRep[][] levels;

		AnonymizationResultRep(AnonymizationResult result) {
			ARXNode active = result.getCurrentNode();
			System.out.println("active: " + Arrays.toString(active.getTransformation()));
			ARXLattice lattice = result.getArxResult().getLattice();
			levels = new ArxNodeRep[lattice.getLevels().length][];
			for (int i = 0; i < levels.length; i++) {
				ARXNode[] nodes = lattice.getLevels()[i];
				levels[i] = new ArxNodeRep[nodes.length];
				for (int j = 0; j < levels[i].length; j++) {
					levels[i][j] = new ArxNodeRep(nodes[j], nodes[j] == active);
				}
			}
		}

		public ArxNodeRep[][] getLevels() {
			return levels;
		}

		public void setLevels(ArxNodeRep[][] levels) {
			this.levels = levels;
		}
	}

	public static class ArxNodeRep {
		private int[] transformation;
		private Anonymity anonymity;
		private boolean active;

		ArxNodeRep(ARXNode node, boolean active) {
			this.transformation = node.getTransformation();
			this.anonymity = node.getAnonymity();
			this.active = active;
		}

		public int[] getTransformation() {
			return transformation;
		}

		public void setTransformation(int[] transformation) {
			this.transformation = transformation;
		}

		public Anonymity getAnonymity() {
			return anonymity;
		}

		public void setAnonymity(Anonymity anonymity) {
			this.anonymity = anonymity;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}
	}
}
