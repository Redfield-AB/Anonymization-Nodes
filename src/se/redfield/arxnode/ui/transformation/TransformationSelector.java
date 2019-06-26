package se.redfield.arxnode.ui.transformation;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.deidentifier.arx.ARXLattice.ARXNode;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.anonymize.AnonymizationResult;

public class TransformationSelector extends JPanel {
	private static final long serialVersionUID = 1L;

	private JTabbedPane tabs;
	private TransformationGraph graph;
	private TransformationsTable table;
	private TransformationFilterPanel filterPanel;

	private AnonymizationResult model;

	public TransformationSelector() {
		super(new FormLayout("f:600:g", "f:300:g, 5:n, p:n"));

		filterPanel = new TransformationFilterPanel();

		add(createTabs(), CC.rc(1, 1));
		add(filterPanel, CC.rc(3, 1));
	}

	private JTabbedPane createTabs() {
		graph = new TransformationGraph(filterPanel.getFilter());
		table = new TransformationsTable(filterPanel.getFilter());

		tabs = new JTabbedPane();
		tabs.addTab("Graph", new JScrollPane(graph));
		tabs.addTab("List", new JScrollPane(table));
		tabs.setSelectedIndex(1);
		return tabs;
	}

	public void setModel(AnonymizationResult result) {
		this.model = result;
		filterPanel.setResult(result.getArxResult());
		table.getModel().setResult(result);
		graph.setResult(result);
	}

	public AnonymizationResult getModel() {
		return model;
	}

	public ARXNode getSelectedNode() {
		if (tabs.getSelectedIndex() == 0) {
			return graph.getSelected();
		} else {
			if (table.getSelectedRow() > -1) {
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				return table.getModel().getRow(row);
			}
		}
		return null;
	}
}
