package se.redfield.arxnode.nodes;

import java.util.Arrays;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;

import se.redfield.arxnode.anonymize.AnonymizationResult;

public class AnonymizerJsNodeViewVal extends JSONViewContent {

	private int[][] selectedTransformations;

	public void updateFrom(List<AnonymizationResult> results) {
		selectedTransformations = new int[results.size()][];
		for (int i = 0; i < selectedTransformations.length; i++) {
			selectedTransformations[i] = results.get(i).getCurrentNode().getTransformation();
		}
	}

	public void assignTo(List<AnonymizationResult> results) {
		if (selectedTransformations != null) {
			for (int i = 0; i < selectedTransformations.length && i < results.size(); i++) {
				int[] transformation = selectedTransformations[i];
				results.get(i).setTransformation(transformation);
			}
		}
	}

	public int[][] getSelectedTransformations() {
		return selectedTransformations;
	}

	public void setSelectedTransformations(int[][] selectedTransformations) {
		this.selectedTransformations = selectedTransformations;
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
		if (obj instanceof AnonymizerJsNodeViewVal) {
			AnonymizerJsNodeViewVal other = (AnonymizerJsNodeViewVal) obj;
			if (selectedTransformations == null) {
				return other.selectedTransformations == null;
			}
			if (selectedTransformations.length == other.selectedTransformations.length) {
				for (int i = 0; i < selectedTransformations.length; i++) {
					if (!Arrays.equals(selectedTransformations[i], other.selectedTransformations[i])) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return selectedTransformations == null ? 0 : selectedTransformations.hashCode();
	}

}
