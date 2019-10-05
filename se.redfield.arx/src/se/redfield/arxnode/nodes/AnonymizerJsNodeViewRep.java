package se.redfield.arxnode.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
			partitions[i] = new AnonymizationResultRep(results.get(i), i);
		}
		processLevels(results);
	}

	private void processLevels(List<AnonymizationResult> results) {
		levels = new ArrayList<>();
		maxLevel = 0;

		attributes = results.get(0).getArxResult().getGlobalOptimum().getQuasiIdentifyingAttributes();
		for (int i = 0; i < attributes.length; i++) {
			levels.add(new HashSet<>());
		}

		for (AnonymizationResult result : results) {
			for (ARXNode[] level : result.getArxResult().getLattice().getLevels()) {
				for (ARXNode node : level) {
					processTransformation(node.getTransformation());
				}
			}
		}
	}

	private void processTransformation(int[] transformation) {
		for (int i = 0; i < transformation.length; i++) {
			int level = transformation[i];

			levels.get(i).add(level);

			if (level > maxLevel) {
				maxLevel = level;
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
		} catch (Exception e) {
			// ignore
		}

	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AnonymizerJsNodeViewRep) {
			AnonymizerJsNodeViewRep other = (AnonymizerJsNodeViewRep) obj;
			if (partitions == null) {
				return other.partitions == null;
			}
			return Arrays.equals(partitions, other.partitions);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return partitions == null ? 0 : Arrays.hashCode(partitions);
	}

	public static class AnonymizationResultRep {

		private ArxNodeRep[][] levels;

		AnonymizationResultRep(AnonymizationResult result, int partitionIndex) {
			ARXLattice lattice = result.getArxResult().getLattice();
			levels = new ArxNodeRep[lattice.getLevels().length][];
			ARXNode optimum = result.getArxResult().getGlobalOptimum();

			for (int i = 0; i < levels.length; i++) {
				ARXNode[] nodes = lattice.getLevels()[i];
				levels[i] = new ArxNodeRep[nodes.length];
				for (int j = 0; j < levels[i].length; j++) {
					levels[i][j] = new ArxNodeRep(nodes[j], lattice, optimum, partitionIndex);
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
		private boolean optimum;
		private String[] successors;

		ArxNodeRep(ARXNode node, ARXLattice lattice, ARXNode optimum, int partitionIndex) {
			this.transformation = node.getTransformation();
			this.anonymity = node.getAnonymity();
			this.minScore = InfolossScore.createFrom(lattice, node.getLowestScore());
			this.maxScore = InfolossScore.createFrom(lattice, node.getHighestScore());
			this.optimum = node == optimum;
			this.successors = Arrays.stream(node.getSuccessors()).map(n -> generateUID(n, partitionIndex))
					.collect(Collectors.toList()).toArray(new String[] {});
		}

		private String generateUID(ARXNode node, int partitionIndex) {
			StringBuilder sb = new StringBuilder();
			sb.append("node-");
			sb.append(partitionIndex);
			for (int i = 0; i < node.getTransformation().length; i++) {
				sb.append("-");
				sb.append(node.getTransformation()[i]);
			}
			return sb.toString();
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

		public boolean isOptimum() {
			return optimum;
		}

		public void setOptimum(boolean optimum) {
			this.optimum = optimum;
		}

		public String[] getSuccessors() {
			return successors;
		}

		public void setSuccessors(String[] successors) {
			this.successors = successors;
		}
	}
}
