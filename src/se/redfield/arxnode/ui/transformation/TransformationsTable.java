package se.redfield.arxnode.ui.transformation;

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;

public class TransformationsTable extends JTable {
	private static final long serialVersionUID = 1L;

	public TransformationsTable(TransformationFilter filter) {
		super(new TransformationTableModel());

		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		getColumnModel().getColumn(TransformationTableModel.COLUMN_ANONYMITY)
				.setCellRenderer(new AnonymityCellRenderer());
		getColumnModel().getColumn(TransformationTableModel.COLUMN_SELECTED)
				.setCellRenderer(new SelectedCellRenderer());
		getColumnModel().getColumn(TransformationTableModel.COLUMN_SELECTED).setMaxWidth(40);

		TableRowSorter<TransformationTableModel> sorter = new TableRowSorter<TransformationTableModel>(getModel());
		sorter.setSortKeys(Arrays.asList(new SortKey(TransformationTableModel.COLUMN_MIN_SCORE, SortOrder.DESCENDING)));
		setRowSorter(sorter);

		sorter.setRowFilter(new RowFilter<TransformationTableModel, Integer>() {

			@Override
			public boolean include(Entry<? extends TransformationTableModel, ? extends Integer> entry) {
				ARXNode node = entry.getModel().getRow(entry.getIdentifier());
				if (node != null) {
					return filter.isAllowed(node);
				}
				return true;
			}

		});
		filter.addChangeListener(e -> sorter.allRowsChanged());
	}

	@Override
	public TransformationTableModel getModel() {
		return (TransformationTableModel) super.getModel();
	}

	class AnonymityCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			ARXNode node = (ARXNode) value;
			ARXNode optimum = getModel().getResult().getArxResult().getGlobalOptimum();

			Anonymity anon = node.getAnonymity();
			setText(anon.toString());
			setIcon(TransformationColors.iconFor(node, optimum));

			return this;
		}

	}

	private class SelectedCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		private Color selectedBg = new Color(128, 255, 255);

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if ((boolean) value) {
				setText("\u2713");
				setHorizontalAlignment(SwingConstants.CENTER);
				setBackground(selectedBg);
			} else {
				setText("");
				setBackground(table.getBackground());
			}
			return this;
		}
	}
}
