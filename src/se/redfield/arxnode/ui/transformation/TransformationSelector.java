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
	private TransformationFilterPanel filterPanel;

	private AnonymizationResult model;

	public TransformationSelector() {
		super(new FormLayout("f:p:g", "f:300:g, 5:n, p:n"));

		filterPanel = new TransformationFilterPanel();
		table = new TransformationsTable(filterPanel.getFilter());

		add(new JScrollPane(table), CC.rc(1, 1));
		add(filterPanel, CC.rc(3, 1));
	}

	public void setModel(AnonymizationResult result) {
		this.model = result;
		filterPanel.setResult(result.getArxResult());
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
