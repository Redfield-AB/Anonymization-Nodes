package se.redfield.arxnode.nodes;

import org.knime.core.node.wizard.AbstractWizardNodeView;

public class AnonymizerJsNodeView
		extends AbstractWizardNodeView<AnonymizerJsNodeModel, AnonymizerJsNodeViewRep, AnonymizerJsNodeViewVal> {

	protected AnonymizerJsNodeView(AnonymizerJsNodeModel nodeModel) {
		super(nodeModel);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void closeView() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean viewInteractionPossible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean validateCurrentValueInView() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected String retrieveCurrentValueFromView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void showValidationErrorInView(String error) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean showApplyOptionsDialog(boolean showDiscardOption, String title, String message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void respondToViewRequest(String response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pushRequestUpdate(String monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void modelChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void callOpenView(String title) {
		// TODO Auto-generated method stub

	}

}
