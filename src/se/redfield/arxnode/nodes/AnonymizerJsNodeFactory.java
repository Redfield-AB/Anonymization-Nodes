package se.redfield.arxnode.nodes;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.interactive.InteractiveNodeFactoryExtension;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;

public class AnonymizerJsNodeFactory extends NodeFactory<AnonymizerJsNodeModel>
		implements WizardNodeFactoryExtension<AnonymizerJsNodeModel, AnonymizerJsNodeViewRep, AnonymizerJsNodeViewVal>,
		InteractiveNodeFactoryExtension<AnonymizerJsNodeModel, AnonymizerJsNodeViewRep, AnonymizerJsNodeViewVal> {

	@Override
	public AnonymizerJsNodeModel createNodeModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getNrNodeViews() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public NodeView<AnonymizerJsNodeModel> createNodeView(int viewIndex, AnonymizerJsNodeModel nodeModel) {
		// TODO Auto-generated method stub
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

	@SuppressWarnings("unchecked")
	@Override
	public AnonymizerJsNodeView createInteractiveView(AnonymizerJsNodeModel model) {
		return new AnonymizerJsNodeView(model);
	}

}
