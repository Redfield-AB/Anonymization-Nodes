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
package se.redfield.arxnode.hierarchy.edit;

import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;
import javax.swing.table.AbstractTableModel;

import org.deidentifier.arx.DataType;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class HierarchyOrderBasedEditor<T> extends JPanel {
	private static final long serialVersionUID = 1L;

	private HierarchyModelOrder<T> model;
	private JTable table;
	private AbstractTableModel tableModel;
	private JComboBox<String> cbSort;

	public HierarchyOrderBasedEditor(HierarchyModelOrder<T> model) {
		this.model = model;
		initUI();
	}

	private void initUI() {
		setLayout(new FormLayout("f:p:g", "f:p:g"));
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createOrderingPanel(), createGroupsPanel());
		add(split, CC.rc(1, 1));
	}

	private JPanel createGroupsPanel() {
		HierarchyGroupBasedEditor<T> editor = new HierarchyGroupBasedEditor<>(model);
		editor.setBorder(BorderFactory.createTitledBorder("Groups"));
		return editor;
	}

	private JPanel createOrderingPanel() {
		JButton bUp = new JButton("Move Up");
		bUp.addActionListener(e -> onMove(true));
		JButton bDown = new JButton("Move Down");
		bDown.addActionListener(e -> onMove(false));

		JPanel panel = new JPanel(new FormLayout("150:g", "f:200:g, 5:n, p:n, 5:n, p:n, 5:n, p:n"));
		panel.add(createOrderingTable(), CC.rc(1, 1));
		panel.add(bUp, CC.rc(3, 1));
		panel.add(bDown, CC.rc(5, 1));
		panel.add(createSortCombo(), CC.rc(7, 1));
		panel.setBorder(BorderFactory.createTitledBorder("Order"));
		return panel;
	}

	private JComboBox<String> createSortCombo() {
		cbSort = new JComboBox<>();
		cbSort.addItem("Custom");
		cbSort.addItem(model.getDataType().getDescription().getLabel());
		cbSort.addActionListener(e -> onSort(cbSort.getSelectedIndex()));
		cbSort.setBorder(BorderFactory.createTitledBorder("Sort"));
		return cbSort;
	}

	private void onSort(int index) {
		if (index > 0) {
			DataType<?> type = model.getDataType();
			if (!model.sort(type)) {
				model.sort(DataType.STRING);
			}
			tableModel.fireTableDataChanged();
		}
	}

	private void onMove(boolean up) {
		int index = table.getSelectedRow();
		if (index > -1) {
			int count = table.getSelectedRowCount();
			boolean res = up ? model.moveUp(index, count) : model.moveDown(index, count);
			if (res) {
				tableModel.fireTableDataChanged();
				index = up ? index - 1 : index + 1;
				table.getSelectionModel().setSelectionInterval(index, index + count - 1);
				cbSort.setSelectedIndex(0);
			}
		}

	}

	private JComponent createOrderingTable() {
		tableModel = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return model.getData()[rowIndex];
			}

			@Override
			public int getRowCount() {
				return model.getData().length;
			}

			@Override
			public int getColumnCount() {
				return 1;
			}

			@Override
			public String getColumnName(int column) {
				return "Values";
			}
		};

		table = new JTable(tableModel);
		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);
		table.setTransferHandler(new OrderTransferHandler());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

		return new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	}

	private class OrderTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 1L;
		private static final String SEPARATOR = "-";

		@Override
		public boolean canImport(TransferSupport support) {
			return support.isDataFlavorSupported(DataFlavor.stringFlavor);
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			JTable t = (JTable) c;
			return new StringSelection(t.getSelectedRow() + SEPARATOR + t.getSelectedRowCount());
		}

		@Override
		public int getSourceActions(JComponent c) {
			return TransferHandler.MOVE;
		}

		@Override
		public boolean importData(TransferSupport support) {
			JTable.DropLocation dl = (javax.swing.JTable.DropLocation) support.getDropLocation();
			try {
				int dstRow = dl.getRow();
				String[] data = ((String) support.getTransferable().getTransferData(DataFlavor.stringFlavor))
						.split(SEPARATOR);
				int srcRow = Integer.parseInt(data[0]);
				int count = Integer.parseInt(data[1]);
				onMove(srcRow, dstRow, count);
			} catch (Exception e) {
				// ignore
			}
			return super.importData(support);
		}

		private void onMove(int from, int to, int count) {
			boolean res = model.move(from, to, count);
			if (res) {
				tableModel.fireTableDataChanged();
				cbSort.setSelectedIndex(0);
			}
		}
	}

}
