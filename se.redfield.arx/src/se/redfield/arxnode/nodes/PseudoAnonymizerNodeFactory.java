package se.redfield.arxnode.nodes;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class PseudoAnonymizerNodeFactory extends NodeFactory<PseudoAnonymizerNodeModel> {

	@Override
	public PseudoAnonymizerNodeModel createNodeModel() {
		return new PseudoAnonymizerNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<PseudoAnonymizerNodeModel> createNodeView(int viewIndex, PseudoAnonymizerNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new PseudoAnonymizerNodeDialog();
	}

}
