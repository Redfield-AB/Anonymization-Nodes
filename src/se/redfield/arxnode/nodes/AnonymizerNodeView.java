package se.redfield.arxnode.nodes;

import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.interactive.DefaultReexecutionCallback;
import org.knime.core.node.interactive.InteractiveClientNodeView;
import org.knime.core.node.interactive.ViewContent;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.anonymize.AnonymizationResult;
import se.redfield.arxnode.nodes.AnonymizerNodeView.AnonymizerNodeViewValue;

public class AnonymizerNodeView
		extends InteractiveClientNodeView<AnonymizerNodeModel, AnonymizerNodeViewValue, AnonymizerNodeViewValue> {
	private static final NodeLogger logger = NodeLogger.getLogger(AnonymizerNodeView.class);

	private JButton bRun;
	private JTable table;

	protected AnonymizerNodeView(AnonymizerNodeModel nodeModel) {
		super(nodeModel);
		bRun = new JButton("Apply");
		bRun.addActionListener(e -> execute());
		bRun.setEnabled(false);

		JPanel panel = new JPanel(new FormLayout("10:n, f:p:g, 10:n", "10:n, f:p:g, 5:n, p:n, 10:n"));
		panel.add(new JScrollPane(createTable()), CC.rc(2, 2, "f,f"));
		panel.add(bRun, CC.rc(4, 2, "f, c"));
		setComponent(panel);
	}

	private JTable createTable() {
		table = new JTable(new TransformationTableModel());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(e -> bRun.setEnabled(table.getSelectedRow() > -1));
		return table;
	}

	private void execute() {
		ARXNode node = null;
		if (table.getSelectedRow() >= 0) {
			node = ((TransformationTableModel) table.getModel()).getRow(table.getSelectedRow());
		}
		AnonymizerNodeViewValue val = new AnonymizerNodeViewValue(node == null ? null : node.getTransformation());
		triggerReExecution(val, true, new DefaultReexecutionCallback());
	}

	@Override
	protected void onClose() {
		logger.debug("onClose");
	}

	@Override
	protected void onOpen() {
		logger.debug("onOpen");
	}

	@Override
	protected void modelChanged() {
		logger.debug("modelChanged");
		List<AnonymizationResult> results = getNodeModel().getResults();
		ARXResult result = null;
		if (results != null && results.size() > 0) {
			result = results.get(0).getArxResult();
		}

		int[] currentTransform = getNodeModel().getSelectedTransformation();
		if (currentTransform == null && result != null) {
			currentTransform = result.getGlobalOptimum().getTransformation();
		}

		TransformationTableModel model = ((TransformationTableModel) table.getModel());
		model.setResult(result);
		model.setCurrentTransform(currentTransform);
	}

	private class TransformationTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private static final int COLUMN_SELECTED = 0;
		private static final int COLUMN_TRANSFROMATION = 1;
		private static final int COLUMN_ANONYMITY = 2;

		private ARXResult result;
		private int[] currentTransform;

		public void setResult(ARXResult result) {
			this.result = result;
			fireTableDataChanged();
		}

		public void setCurrentTransform(int[] currentTransform) {
			this.currentTransform = currentTransform;
			fireTableDataChanged();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			if (result == null) {
				return 0;
			}
			int count = 0;
			for (ARXNode[] level : result.getLattice().getLevels()) {
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
					return node.getAnonymity().toString();
				}
			}
			return null;

		}

		private String getSelectedVal(ARXNode node) {
			if (Arrays.equals(currentTransform, node.getTransformation())) {
				return "\u2713";
			}
			return "";
		}

		public ARXNode getRow(int index) {
			if (result == null) {
				return null;
			}
			for (ARXNode[] level : result.getLattice().getLevels()) {
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
			}
			return "";
		}
	}

	public static class AnonymizerNodeViewValue implements ViewContent {
		private int[] transformation;

		public AnonymizerNodeViewValue(int[] transformation) {
			this.transformation = transformation;
		}

		public int[] getTransformation() {
			return transformation;
		}
	}
}
