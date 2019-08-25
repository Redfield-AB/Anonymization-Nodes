package se.redfield.arxnode.nodes;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;

public class AnonymizerJsNodeFactory extends NodeFactory<AnonymizerJsNodeModel>
		implements WizardNodeFactoryExtension<AnonymizerJsNodeModel, AnonymizerJsNodeViewRep, AnonymizerJsNodeViewVal> {

	@Override
	public AnonymizerJsNodeModel createNodeModel() {
		return new AnonymizerJsNodeModel(getInteractiveViewName());
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<AnonymizerJsNodeModel> createNodeView(int viewIndex, AnonymizerJsNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new AnonymizerNodeDialog();
	}

}
