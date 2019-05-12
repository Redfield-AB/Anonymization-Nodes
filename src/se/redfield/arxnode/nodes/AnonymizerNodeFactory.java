package se.redfield.arxnode.nodes;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class AnonymizerNodeFactory extends NodeFactory<AnonymizerNodeModel> {

	@Override
	public AnonymizerNodeModel createNodeModel() {
		return new AnonymizerNodeModel();
	}

	@Override
	public int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<AnonymizerNodeModel> createNodeView(final int viewIndex, final AnonymizerNodeModel nodeModel) {
		return null;
	}

	@Override
	public boolean hasDialog() {
		return true;
	}

	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new AnonymizerNodeDialog();
	}

}
