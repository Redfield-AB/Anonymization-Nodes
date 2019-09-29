package se.redfield.arxnode.nodes;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class HierarchyExpandNodeFactory extends NodeFactory<HierarchyExpandNodeModel> {

	@Override
	public HierarchyExpandNodeModel createNodeModel() {
		return new HierarchyExpandNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<HierarchyExpandNodeModel> createNodeView(int viewIndex, HierarchyExpandNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new HierarchyExpandNodeDialog();
	}

}
