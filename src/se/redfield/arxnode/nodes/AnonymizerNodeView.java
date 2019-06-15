package se.redfield.arxnode.nodes;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.interactive.DefaultReexecutionCallback;
import org.knime.core.node.interactive.InteractiveClientNodeView;
import org.knime.core.node.interactive.ViewContent;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.anonymize.AnonymizationResult;
import se.redfield.arxnode.nodes.AnonymizerNodeView.AnonymizerNodeViewValue;
import se.redfield.arxnode.ui.transformation.TransformationSelector;
import se.redfield.arxnode.ui.transformation.TransformationSelectorsTabbedPane;

public class AnonymizerNodeView
		extends InteractiveClientNodeView<AnonymizerNodeModel, AnonymizerNodeViewValue, AnonymizerNodeViewValue> {
	private static final NodeLogger logger = NodeLogger.getLogger(AnonymizerNodeView.class);

	private JCheckBox cbSingleTransformation;
	private JButton bRun;
	private JButton bReset;
	private TransformationSelectorsTabbedPane selectorsPanel;
	private List<AnonymizationResult> results;

	protected AnonymizerNodeView(AnonymizerNodeModel nodeModel) {
		super(nodeModel);

		selectorsPanel = new TransformationSelectorsTabbedPane();

		JPanel panel = new JPanel(new FormLayout("10:n, f:p:g, 10:n", "10:n, f:p:g, 5:n, p:n, 10:n"));
		panel.add(selectorsPanel, CC.rc(2, 2, "f,f"));
		panel.add(createButtonsPanel(), CC.rc(4, 2, "f, c"));
		setComponent(panel);
	}

	private JPanel createButtonsPanel() {
		bRun = new JButton("Apply Selected");
		bRun.addActionListener(e -> onApply());

		bReset = new JButton("Reset to Optimum");
		bReset.addActionListener(e -> onReset());

		cbSingleTransformation = new JCheckBox("Use the same transformation for all partitions");

		JPanel panel = new JPanel(new FormLayout("p:n, 5:n, p:n, 5:n, p:n, f:p:g", "p:n"));
		panel.add(bRun, CC.rc(1, 1));
		panel.add(bReset, CC.rc(1, 3));
		panel.add(cbSingleTransformation, CC.rc(1, 5));
		return panel;
	}

	private void onApply() {
		if (results == null) {
			return;
		}

		TransformationSelector selector = selectorsPanel.getCurrentSelector();
		ARXNode selected = selector.getSelectedNode();
		AnonymizationResult result = selector.getModel();
		boolean single = true;

		if (selected != null) {
			result.setTransformation(selected.getTransformation());
			if (cbSingleTransformation.isSelected()) {
				single = setForAll(selected.getTransformation());
			}
		} else {
			result.setTransformation(null);
		}

		triggerReExecution(
				new AnonymizerNodeViewValue(single ? null : "Unable to use a single transformation for all partitions"),
				true, new DefaultReexecutionCallback());
	}

	private boolean setForAll(int[] trasformation) {
		boolean single = true;
		for (AnonymizationResult r : results) {
			ARXNode node = r.findNodeForTransfromation(trasformation, false);
			if (node != null) {
				r.setTransformation(node.getTransformation());
			} else {
				r.setTransformation(null);
				single = false;
			}
		}
		return single;
	}

	private void onReset() {
		if (results == null) {
			return;
		}
		for (AnonymizationResult r : results) {
			r.setTransformation(null);
		}
		triggerReExecution(null, true, new DefaultReexecutionCallback());
	}

	@Override
	protected void onClose() {
		logger.debug("onClose");
	}

	@Override
	protected void onOpen() {
		logger.debug("onOpen");
	}

	@Override
	protected void modelChanged() {
		logger.debug("modelChanged");
		results = getNodeModel().getResults();
		selectorsPanel.setModel(results);
		cbSingleTransformation.setVisible(results != null && results.size() > 1);
	}

	public static class AnonymizerNodeViewValue implements ViewContent {
		private String warning;

		public AnonymizerNodeViewValue(String warning) {
			this.warning = warning;
		}

		public String getWarning() {
			return warning;
		}
	}
}
