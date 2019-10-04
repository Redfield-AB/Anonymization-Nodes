package se.redfield.arxnode.ui.transformation;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.knime.core.node.NodeLogger;

public class TransformationFilterTable extends JTable {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(TransformationFilterTable.class);

	public TransformationFilterTable(TransformationFilter filter) {
		super(new TransformationFilterTableModel(filter));
		setRowSelectionAllowed(false);
		setDefaultRenderer(Boolean.class, new LevelCellRenderer());
	}

	@Override
	public TransformationFilterTableModel getModel() {
		return (TransformationFilterTableModel) super.getModel();
	}

	private static class LevelCellRenderer implements TableCellRenderer {
		private JCheckBox renderCb;
		private JLabel nullLabel;

		public LevelCellRenderer() {
			renderCb = new JCheckBox();
			renderCb.setHorizontalAlignment(SwingConstants.CENTER);
			renderCb.setBorderPainted(true);
			renderCb.setBorder(new EmptyBorder(1, 1, 1, 1));

			nullLabel = new JLabel();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (value == null) {
				return nullLabel;
			}
			renderCb.setForeground(table.getForeground());
			renderCb.setBackground(table.getBackground());

			renderCb.setSelected(((Boolean) value).booleanValue());

			return renderCb;
		}

	}

	public static class TransformationFilterTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		public static final int COLUMN_ATTR = 0;
		public static final int COLUMN_LEVEL_0 = 1;

		private TransformationFilter filter;
		private ARXResult result;

		private List<String> attributes;
		private List<Set<Integer>> levels;

		private int columnCount = 0;

		public TransformationFilterTableModel(TransformationFilter filter) {
			this.filter = filter;
			attributes = new ArrayList<>();
			levels = new ArrayList<>();
		}

		public void setResult(ARXResult result) {
			this.result = result;
			init();
		}

		private void init() {
			attributes = new ArrayList<>();
			levels = new ArrayList<>();

			String[] attrs = result.getGlobalOptimum().getQuasiIdentifyingAttributes();
			for (String attr : attrs) {
				attributes.add(attr);
				levels.add(new HashSet<>());
			}

			for (ARXNode[] level : result.getLattice().getLevels()) {
				for (ARXNode node : level) {
					int[] transformation = node.getTransformation();
					for (int i = 0; i < transformation.length; i++) {
						levels.get(i).add(transformation[i]);
					}
				}
			}

			int maxLevel = 0;
			for (Set<Integer> row : levels) {
				for (Integer i : row) {
					if (i > maxLevel) {
						maxLevel = i;
					}
				}
			}
			columnCount = maxLevel + 2;
			fireTableStructureChanged();
			fireTableDataChanged();
		}

		@Override
		public int getColumnCount() {
			return columnCount;
		}

		@Override
		public int getRowCount() {
			return attributes.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == COLUMN_ATTR) {
				return attributes.get(row);
			}
			if (col >= COLUMN_LEVEL_0) {
				int level = col - COLUMN_LEVEL_0;
				if (levels.get(row).contains(level)) {
					return filter.getLevels()[row].contains(level);
				} else {
					return null;
				}
			}
			return null;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex >= COLUMN_LEVEL_0) {
				int level = columnIndex - COLUMN_LEVEL_0;
				filter.setLevelVisible(rowIndex, level, (boolean) aValue);
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex >= COLUMN_LEVEL_0) {
				return Boolean.class;
			}
			return String.class;
		}

		@Override
		public String getColumnName(int column) {
			if (column == COLUMN_ATTR) {
				return "Attribute";
			}
			if (column >= COLUMN_LEVEL_0) {
				return Integer.toString(column - COLUMN_LEVEL_0);
			}
			return super.getColumnName(column);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex >= COLUMN_LEVEL_0 && levels.get(rowIndex).contains(columnIndex - COLUMN_LEVEL_0);
		}
	}
}
