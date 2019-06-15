package se.redfield.arxnode.ui.transformation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
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

	static class AnonymityCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		private static final Map<Anonymity, ImageIcon> icons = new HashMap<>();
		static {
			icons.put(Anonymity.ANONYMOUS, createIcon(Color.GREEN));
			icons.put(Anonymity.NOT_ANONYMOUS, createIcon(Color.RED));
			icons.put(Anonymity.PROBABLY_ANONYMOUS, createIcon(new Color(150, 255, 150)));
			icons.put(Anonymity.PROBABLY_NOT_ANONYMOUS, createIcon(new Color(255, 150, 150)));
			icons.put(Anonymity.UNKNOWN, createIcon(Color.GRAY));
		}

		private static ImageIcon createIcon(Color color) {
			BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = image.createGraphics();
			graphics.setPaint(color);
			graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
			return new ImageIcon(image);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			Anonymity anon = (Anonymity) value;
			setText(anon.toString());
			setIcon(icons.get(anon));

			return this;
		}

	}
}
