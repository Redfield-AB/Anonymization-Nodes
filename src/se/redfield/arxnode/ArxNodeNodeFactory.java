package se.redfield.arxnode;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class ArxNodeNodeFactory extends NodeFactory<ArxNodeNodeModel> {

	@Override
	public ArxNodeNodeModel createNodeModel() {
		return new ArxNodeNodeModel();
	}

	@Override
	public int getNrNodeViews() {
		return 1;
	}

	@Override
	public NodeView<ArxNodeNodeModel> createNodeView(final int viewIndex, final ArxNodeNodeModel nodeModel) {
		return new ArxNodeNodeView(nodeModel);
	}

	@Override
	public boolean hasDialog() {
		return true;
	}

	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new ArxNodeNodeDialog();
	}

}
