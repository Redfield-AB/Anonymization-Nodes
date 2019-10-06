/*
 * Copyright (c) 2019 Redfield AB.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *  
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package se.redfield.arxnode.ui.transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;

public class TransformationFilter {

	private Set<Anonymity> anonymity;
	private double minScore;
	private double maxScore;
	private Set<Integer>[] levels;

	private ARXResult result;
	private List<ChangeListener> listeners = new ArrayList<>();

	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	public void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}

	protected void fireChangeEvent(ChangeEvent e) {
		for (ChangeListener l : listeners) {
			l.stateChanged(e);
		}
	}

	protected void fireChange() {
		fireChangeEvent(new ChangeEvent(this));
	}

	public Set<Anonymity> getAnonymity() {
		return anonymity;
	}

	public void setAnonymity(boolean include, Anonymity... modes) {
		int prevSize = anonymity.size();

		if (include) {
			anonymity.addAll(Arrays.asList(modes));
		} else {
			anonymity.removeAll(Arrays.asList(modes));
		}

		if (prevSize != anonymity.size()) {
			fireChange();
		}
	}

	public double getMinScore() {
		return minScore;
	}

	public void setMinScore(double minScore) {
		if (this.minScore != minScore) {
			this.minScore = minScore;
			fireChange();
		}
	}

	public double getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(double maxScore) {
		if (this.maxScore != maxScore) {
			this.maxScore = maxScore;
			fireChange();
		}
	}

	public Set<Integer>[] getLevels() {
		return levels;
	}

	public void setLevelVisible(int attr, int level, boolean visible) {
		int prevSize = levels[attr].size();

		if (visible) {
			levels[attr].add(level);
		} else {
			levels[attr].remove(level);
		}

		if (prevSize != levels[attr].size()) {
			fireChange();
		}
	}

	public void setResult(ARXResult result) {
		if (this.result == null || levels == null
				|| levels.length != result.getGlobalOptimum().getTransformation().length) {
			init(result);
		}
	}

	@SuppressWarnings("unchecked")
	private void init(ARXResult result) {
		this.result = result;
		minScore = 0d;
		maxScore = 1d;

		anonymity = new HashSet<>();
		anonymity.addAll(Arrays.asList(Anonymity.ANONYMOUS, Anonymity.PROBABLY_ANONYMOUS, Anonymity.UNKNOWN));

		levels = new Set[result.getGlobalOptimum().getTransformation().length];
		for (int i = 0; i < levels.length; i++) {
			levels[i] = new HashSet<>();
		}

		for (ARXNode[] nodeLevels : result.getLattice().getLevels()) {
			for (ARXNode node : nodeLevels) {
				int[] transform = node.getTransformation();
				for (int i = 0; i < transform.length; i++) {
					levels[i].add(transform[i]);
				}
			}
		}
		fireChange();
	}

	public boolean isAllowed(ARXNode node) {
		if (result == null) {
			return true;
		}
		double max = node.getHighestScore().relativeTo(result.getLattice().getLowestScore(),
				result.getLattice().getHighestScore());
		double min = node.getLowestScore().relativeTo(result.getLattice().getLowestScore(),
				result.getLattice().getHighestScore());

		if (max < minScore || min > maxScore || !anonymity.contains(node.getAnonymity())) {
			return false;
		}

		int[] transformation = node.getTransformation();
		for (int i = 0; i < transformation.length; i++) {
			if (!levels[i].contains(transformation[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "Min: " + minScore + "\nMax: " + maxScore + "\nModes: " + Arrays.toString(anonymity.toArray())
				+ "\nLevels:" + printLevels();
	}

	private String printLevels() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < levels.length; i++) {
			sb.append("\n" + i + ": " + Arrays.toString(levels[i].toArray()));
		}
		return sb.toString();
	}
}
