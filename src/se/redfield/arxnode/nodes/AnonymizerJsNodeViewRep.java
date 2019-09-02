package se.redfield.arxnode.nodes;

import java.util.List;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import se.redfield.arxnode.anonymize.AnonymizationResult;
import se.redfield.arxnode.ui.transformation.InfolossScore;

public class AnonymizerJsNodeViewRep extends JSONViewContent {
	public static final String CONFIG_VIEW_REP = "viewRep";

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
		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(partitions);
		// System.out.println("json");
		// System.out.println(json);
		settings.addString(CONFIG_VIEW_REP, json);
	}

	@Override
	public void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		String json = settings.getString(CONFIG_VIEW_REP, null);
		if (json != null) {
			Gson gson = new GsonBuilder().create();
			partitions = gson.fromJson(json, AnonymizationResultRep[].class);
		}
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
			ARXLattice lattice = result.getArxResult().getLattice();
			levels = new ArxNodeRep[lattice.getLevels().length][];
			for (int i = 0; i < levels.length; i++) {
				ARXNode[] nodes = lattice.getLevels()[i];
				levels[i] = new ArxNodeRep[nodes.length];
				for (int j = 0; j < levels[i].length; j++) {
					levels[i][j] = new ArxNodeRep(nodes[j], lattice);
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
		private InfolossScore minScore;
		private InfolossScore maxScore;

		ArxNodeRep(ARXNode node, ARXLattice lattice) {
			this.transformation = node.getTransformation();
			this.anonymity = node.getAnonymity();
			this.minScore = InfolossScore.createFrom(lattice, node.getLowestScore());
			this.maxScore = InfolossScore.createFrom(lattice, node.getHighestScore());
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

		public InfolossScore getMinScore() {
			return minScore;
		}

		public void setMinScore(InfolossScore minScore) {
			this.minScore = minScore;
		}

		public InfolossScore getMaxScore() {
			return maxScore;
		}

		public void setMaxScore(InfolossScore maxScore) {
			this.maxScore = maxScore;
		}
	}
}
