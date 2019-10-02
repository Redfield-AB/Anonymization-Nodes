package se.redfield.arxnode.nodes;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class HierarchyCreateNodeFactory extends NodeFactory<HierarchyCreateNodeModel> {

	@Override
	public HierarchyCreateNodeModel createNodeModel() {
		return new HierarchyCreateNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<HierarchyCreateNodeModel> createNodeView(int viewIndex, HierarchyCreateNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new HierarchyCreateNodeDialog();
	}

}
