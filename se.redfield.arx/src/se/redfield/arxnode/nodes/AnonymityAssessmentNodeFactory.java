package se.redfield.arxnode.nodes;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class AnonymityAssessmentNodeFactory extends NodeFactory<AnonymityAssessmentNodeModel> {

	@Override
	public AnonymityAssessmentNodeModel createNodeModel() {
		return new AnonymityAssessmentNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<AnonymityAssessmentNodeModel> createNodeView(int viewIndex,
			AnonymityAssessmentNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new AnonymityAssessmentNodeDialog();
	}

}
