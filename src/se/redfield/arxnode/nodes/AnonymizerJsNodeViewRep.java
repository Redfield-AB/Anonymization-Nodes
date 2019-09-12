package se.redfield.arxnode.nodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private String[] attributes;
	private List<Set<Integer>> levels;
	private int maxLevel;

	public void updateFrom(List<AnonymizationResult> results) {
		partitions = new AnonymizationResultRep[results.size()];
		for (int i = 0; i < partitions.length; i++) {
			partitions[i] = new AnonymizationResultRep(results.get(i));
		}
		processLevels(results);
	}

	private void processLevels(List<AnonymizationResult> results) {
		levels = new ArrayList<>();

		attributes = results.get(0).getArxResult().getGlobalOptimum().getQuasiIdentifyingAttributes();
		for (int i = 0; i < attributes.length; i++) {
			levels.add(new HashSet<>());
		}

		for (AnonymizationResult result : results) {
			for (ARXNode[] level : result.getArxResult().getLattice().getLevels()) {
				for (ARXNode node : level) {
					int[] transformation = node.getTransformation();
					for (int i = 0; i < transformation.length; i++) {
						levels.get(i).add(transformation[i]);
					}
				}
			}
		}

		maxLevel = 0;
		for (Set<Integer> row : levels) {
			for (Integer i : row) {
				if (i > maxLevel) {
					maxLevel = i;
				}
			}
		}

	}

	public AnonymizationResultRep[] getPartitions() {
		return partitions;
	}

	public void setPartitions(AnonymizationResultRep[] partitions) {
		this.partitions = partitions;
	}

	public String[] getAttributes() {
		return attributes;
	}

	public void setAttributes(String[] attributes) {
		this.attributes = attributes;
	}

	public List<Set<Integer>> getLevels() {
		return levels;
	}

	public void setLevels(List<Set<Integer>> levels) {
		this.levels = levels;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	@Override
	public void saveToNodeSettings(NodeSettingsWO settings) {
		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(this);
		// System.out.println("rep.json");
		// System.out.println(json);
		settings.addString(CONFIG_VIEW_REP, json);
	}

	@Override
	public void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		String json = settings.getString(CONFIG_VIEW_REP, null);
		Gson gson = new GsonBuilder().create();
		try {
			AnonymizerJsNodeViewRep temp = gson.fromJson(json, AnonymizerJsNodeViewRep.class);
			this.partitions = temp.partitions;
			this.attributes = temp.attributes;
			this.levels = temp.levels;
			this.maxLevel = temp.maxLevel;
		} catch (Throwable e) {

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
