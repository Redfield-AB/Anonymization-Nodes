package se.redfield.arxnode.ui.transformation;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deidentifier.arx.ARXLattice.ARXNode;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.anonymize.AnonymizationResult;

public class TransformationSelector extends JPanel {
	private static final long serialVersionUID = 1L;

	private TransformationsTable table;

	private AnonymizationResult model;

	public TransformationSelector() {
		super(new FormLayout("f:p:g", "f:p:g"));

		table = new TransformationsTable();

		add(new JScrollPane(table), CC.rc(1, 1));
	}

	public void setModel(AnonymizationResult result) {
		this.model = result;
		table.getModel().setResult(result);
	}

	public AnonymizationResult getModel() {
		return model;
	}

	public ARXNode getSelectedNode() {
		if (table.getSelectedRow() > -1) {
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			return table.getModel().getRow(row);
		}
		return null;
	}
}
