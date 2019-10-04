package se.redfield.arxnode.nodes;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import se.redfield.arxnode.anonymize.AnonymizationResult;

public class AnonymizerJsNodeViewVal extends JSONViewContent {

	public static final String CONFIG_VIEW_VAL = "viewVal";
	public static final String CONFIG_VIEW_STATE = "viewState";

	private int[][] selectedTransformations;
	private ViewState state;

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

	public ViewState getState() {
		return state;
	}

	public void setState(ViewState state) {
		this.state = state;
	}

	@Override
	public void saveToNodeSettings(NodeSettingsWO settings) {
		Gson gson = new GsonBuilder().create();
		settings.addString(CONFIG_VIEW_VAL, gson.toJson(selectedTransformations));
		settings.addString(CONFIG_VIEW_STATE, gson.toJson(state));
	}

	@Override
	public void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		String json = settings.getString(CONFIG_VIEW_VAL, null);
		Gson gson = new GsonBuilder().create();
		if (json != null) {
			selectedTransformations = gson.fromJson(json, int[][].class);
		}
		json = settings.getString(CONFIG_VIEW_STATE, null);
		if (json != null) {
			state = gson.fromJson(json, ViewState.class);
		}
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
			}
			return ObjectUtils.equals(state, other.state);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = 0;
		if (selectedTransformations != null) {
			result += Arrays.hashCode(selectedTransformations);
		}
		if (state != null) {
			result = result * 31 + state.hashCode();
		}

		return result;
	}

	static class ViewState {
		private FilterState filter;
		private PartitionState[] partitions;

		public FilterState getFilter() {
			return filter;
		}

		public void setFilter(FilterState filter) {
			this.filter = filter;
		}

		public PartitionState[] getPartitions() {
			return partitions;
		}

		public void setPartitions(PartitionState[] partitions) {
			this.partitions = partitions;
		}

		@Override
		public int hashCode() {
			return filter.hashCode() * 31 + Arrays.hashCode(partitions);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ViewState) {
				ViewState other = (ViewState) obj;
				return ObjectUtils.equals(filter, other.filter) && ObjectUtils.equals(partitions, other.partitions);
			}
			return false;
		}
	}

	static class FilterState {
		private boolean panelExpanded;
		private boolean[] anonymity;
		private boolean[][] levels;
		private FilterScore score;

		public boolean isPanelExpanded() {
			return panelExpanded;
		}

		public void setPanelExpanded(boolean panelExpanded) {
			this.panelExpanded = panelExpanded;
		}

		public boolean[] getAnonymity() {
			return anonymity;
		}

		public void setAnonymity(boolean[] anonymity) {
			this.anonymity = anonymity;
		}

		public boolean[][] getLevels() {
			return levels;
		}

		public void setLevels(boolean[][] levels) {
			this.levels = levels;
		}

		public FilterScore getScore() {
			return score;
		}

		public void setScore(FilterScore score) {
			this.score = score;
		}

		@Override
		public int hashCode() {
			HashCodeBuilder b = new HashCodeBuilder();
			b.append(panelExpanded);
			b.append(anonymity);
			b.append(levels);
			b.append(score);
			return b.toHashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FilterState) {
				FilterState other = (FilterState) obj;
				return panelExpanded == other.panelExpanded && ObjectUtils.equals(anonymity, other.anonymity)
						&& ObjectUtils.equals(levels, other.levels) && ObjectUtils.equals(score, other.score);
			}
			return false;
		}
	}

	static class FilterScore {
		private int min;
		private int max;

		public int getMin() {
			return min;
		}

		public void setMin(int min) {
			this.min = min;
		}

		public int getMax() {
			return max;
		}

		public void setMax(int max) {
			this.max = max;
		}

		@Override
		public int hashCode() {
			return max * 31 + min;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FilterScore) {
				FilterScore other = (FilterScore) obj;
				return min == other.min && max == other.max;
			}
			return false;
		}
	}

	static class PartitionState {
		private boolean active;
		private boolean tableView;
		private String table;
		private String graph;

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public boolean isTableView() {
			return tableView;
		}

		public void setTableView(boolean tableView) {
			this.tableView = tableView;
		}

		public String getTable() {
			return table;
		}

		public void setTable(String table) {
			this.table = table;
		}

		public String getGraph() {
			return graph;
		}

		public void setGraph(String graph) {
			this.graph = graph;
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(active).append(tableView).append(table).append(graph).toHashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PartitionState) {
				PartitionState other = (PartitionState) obj;
				return active == other.active && tableView == other.tableView && ObjectUtils.equals(table, other.table)
						&& ObjectUtils.equals(graph, other.graph);
			}
			return false;
		}
	}
}
