package se.redfield.arxnode.ui.transformation;

import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.knime.core.node.NodeLogger;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.anonymize.AnonymizationResult;

public class TransformationSelectorsTabbedPane extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final NodeLogger logger = NodeLogger.getLogger(TransformationSelectorsTabbedPane.class);

	private JTabbedPane tabs;
	private JLabel errorLabel;

	public TransformationSelectorsTabbedPane() {
		super(new FormLayout("f:p:g", "f:p:g"));

		tabs = new JTabbedPane();
		tabs.setTabPlacement(JTabbedPane.LEFT);

		errorLabel = new JLabel("No transformations available. Prease re-execute the node");
		errorLabel.setHorizontalAlignment(JLabel.CENTER);
		errorLabel.setVerticalAlignment(JLabel.CENTER);
	}

	public void setModel(List<AnonymizationResult> results) {
		if (results == null || results.size() == 0) {
			setContent(errorLabel);
			return;
		}

		adjustTabCount(results.size());
		for (int i = 0; i < results.size(); i++) {
			getSelector(i).setModel(results.get(i));
		}

		setContent(tabs.getTabCount() > 1 ? tabs : tabs.getComponentAt(0));
	}

	private void setContent(Component component) {
		removeAll();
		add(component, CC.rc(1, 1));
		updateUI();
	}

	private void adjustTabCount(int count) {
		if (count > tabs.getTabCount()) {
			for (int i = tabs.getTabCount(); i < count; i++) {
				tabs.addTab(String.valueOf(i), new TransformationSelector());
			}
		} else {
			for (int i = tabs.getTabCount() - 1; i >= count; i--) {
				tabs.removeTabAt(i);
			}
		}
	}

	private TransformationSelector getSelector(int i) {
		return (TransformationSelector) tabs.getComponentAt(i);
	}

	public TransformationSelector getCurrentSelector() {
		return getSelector(tabs.getSelectedIndex());
	}
}
