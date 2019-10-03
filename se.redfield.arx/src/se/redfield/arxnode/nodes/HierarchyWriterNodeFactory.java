package se.redfield.arxnode.nodes;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class HierarchyWriterNodeFactory extends NodeFactory<HierarchyWriterNodeModel> {

	@Override
	public HierarchyWriterNodeModel createNodeModel() {
		return new HierarchyWriterNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<HierarchyWriterNodeModel> createNodeView(int viewIndex, HierarchyWriterNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new HierarchyWriterNodeDialog();
	}

}
