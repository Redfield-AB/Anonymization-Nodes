package se.redfield.arxnode.ui;

import javax.swing.JLabel;

import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.arxnode.nodes.ArxPortObjectSpec;

public class HierarchyOverwriteNoticeLabel extends JLabel {
	private static final long serialVersionUID = 1L;

	private SettingsModelString model;
	private ArxPortObjectSpec arxObject;

	public HierarchyOverwriteNoticeLabel(SettingsModelString model) {
		this.model = model;
		model.addChangeListener(e -> updateNotice());
	}

	private void updateNotice() {
		if (arxObject != null) {
			if (arxObject.getHierarchies().contains(model.getStringValue())) {
				setText("<html><font color='red'>"
						+ "Hierarchy for this column is already present and will be overwriten.</font></html>");
			} else {
				setText("");
			}
		}
	}

	public void setArxObject(ArxPortObjectSpec arxObject) {
		this.arxObject = arxObject;
		updateNotice();
	}
}
