package se.redfield.arxnode.ui.transformation;

import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;

import se.redfield.arxnode.anonymize.AnonymizationResult;

public class TransformationTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	public static final int COLUMN_SELECTED = 0;
	public static final int COLUMN_TRANSFROMATION = 1;
	public static final int COLUMN_ANONYMITY = 2;
	public static final int COLUMN_MIN_SCORE = 3;
	public static final int COLUMN_MAX_SCORE = 4;

	private AnonymizationResult result;

	public void setResult(AnonymizationResult result) {
		this.result = result;
		fireTableDataChanged();
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public int getRowCount() {
		if (result == null) {
			return 0;
		}
		int count = 0;
		for (ARXNode[] level : result.getArxResult().getLattice().getLevels()) {
			count += level.length;
		}
		return count;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ARXNode node = getRow(rowIndex);
		if (node != null) {
			switch (columnIndex) {
			case COLUMN_SELECTED:
				return getSelectedVal(node);
			case COLUMN_TRANSFROMATION:
				return Arrays.toString(node.getTransformation());
			case COLUMN_ANONYMITY:
				return node.getAnonymity();
			case COLUMN_MIN_SCORE:
				return InfolossScore.createFrom(result.getArxResult().getLattice(), node.getLowestScore());
			case COLUMN_MAX_SCORE:
				return InfolossScore.createFrom(result.getArxResult().getLattice(), node.getHighestScore());
			}
		}
		return null;

	}

	private String getSelectedVal(ARXNode node) {
		if (Arrays.equals(result.getTransformation(), node.getTransformation())) {
			return "\u2713";
		}
		return "";
	}

	public ARXNode getRow(int index) {
		if (result == null) {
			return null;
		}
		for (ARXNode[] level : result.getArxResult().getLattice().getLevels()) {
			if (index >= level.length) {
				index -= level.length;
			} else {
				return level[index];
			}
		}
		return null;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case COLUMN_SELECTED:
			return "Active";
		case COLUMN_TRANSFROMATION:
			return "Transformation";
		case COLUMN_ANONYMITY:
			return "Anonymity";
		case COLUMN_MIN_SCORE:
			return "Min Score";
		case COLUMN_MAX_SCORE:
			return "Max Score";
		}
		return "";
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case COLUMN_ANONYMITY:
			return Anonymity.class;
		case COLUMN_MAX_SCORE:
		case COLUMN_MIN_SCORE:
			return InfolossScore.class;
		}
		return String.class;
	}
}