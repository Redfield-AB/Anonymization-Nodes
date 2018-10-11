package se.redfield.arxnode;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.config.DPresenceConfig;
import se.redfield.arxnode.config.KAnonymityConfig;
import se.redfield.arxnode.config.PrivacyModelConfig;
import se.redfield.arxnode.config.PrivacyModelEditor;
import se.redfield.arxnode.util.PopupMenuButton;

public class PrivacyModelsPane {
	// private static final NodeLogger logger =
	// NodeLogger.getLogger(PrivacyModelsPane.class);

	private Config config;

	private JPanel container;
	private JPanel editPanel;
	private JPanel editComponentContainer;

	private JList<PrivacyModelConfig> list;
	private PrivacyListModel model;

	private PrivacyModelConfig currentConfig;
	private PrivacyModelEditor currentEditor;
	private boolean isNew;

	public PrivacyModelsPane(Config config) {
		this.config = config;
	}

	public JPanel getComponent() {
		if (container == null) {
			initUI();
		}
		return container;
	}

	private void initUI() {
		container = new JPanel();
		JPanel listPanel = createListPanel();

		CellConstraints cc = new CellConstraints();
		container.setLayout(new FormLayout("f:p:g", "p:n, 5:n, f:p:g"));
		container.add(createEditPanel(), cc.rc(1, 1));
		container.add(listPanel, cc.rc(3, 1));
	}

	private JPanel createEditPanel() {
		JButton bSave = new JButton("Save");
		bSave.addActionListener(e -> onSave());
		JButton bCancel = new JButton("Cancel");
		bCancel.addActionListener(e -> cancelEdit());

		editComponentContainer = new JPanel();

		CellConstraints cc = new CellConstraints();
		editPanel = new JPanel(new FormLayout("f:p:g, p:n, 5:n, p:n, f:p:g", "f:p:g, 5:n, p:n"));
		editPanel.add(editComponentContainer, cc.rcw(1, 1, 5));
		editPanel.add(bSave, cc.rc(3, 2));
		editPanel.add(bCancel, cc.rc(3, 4));

		editPanel.setVisible(false);
		return editPanel;
	}

	private void onSave() {
		currentEditor.readFromComponent(currentConfig);
		if (isNew) {
			config.getPrivacyModels().add(currentConfig);
		}
		model.fireUpdate();
		cancelEdit();
	}

	private void cancelEdit() {
		editPanel.setVisible(false);
	}

	private JPanel createListPanel() {
		list = new JList<>();
		model = new PrivacyListModel();
		list.setModel(model);

		JMenuItem kAnonymityItem = new JMenuItem("KAnonyminy");
		kAnonymityItem.addActionListener(e -> edit(new KAnonymityConfig(), true));
		JMenuItem dPresenceItem = new JMenuItem("DPresence");
		dPresenceItem.addActionListener(e -> edit(new DPresenceConfig(), true));
		JPopupMenu menu = new JPopupMenu();
		menu.add(kAnonymityItem);
		menu.add(dPresenceItem);
		JButton bAdd = new PopupMenuButton("Add", menu);

		JButton bEdit = new JButton("Edit");
		bEdit.addActionListener(e -> onEdit());
		JButton bRemove = new JButton("Remove");
		bRemove.addActionListener(e -> onRemove());

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();

		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridheight = 4;
		gc.weightx = 0.9;
		gc.weighty = 1;
		gc.fill = GridBagConstraints.BOTH;
		panel.add(list, gc);

		gc.gridheight = 1;
		gc.weightx = 0;
		gc.weighty = 0;
		gc.gridx = 1;
		gc.gridy = 0;
		panel.add(bAdd, gc);
		gc.gridx = 1;
		gc.gridy = 1;
		panel.add(bEdit, gc);
		gc.gridx = 1;
		gc.gridy = 2;
		panel.add(bRemove, gc);
		panel.setBorder(BorderFactory.createTitledBorder("Privacy Models"));

		return panel;
	}

	private void onEdit() {
		PrivacyModelConfig selected = list.getSelectedValue();
		if (selected != null) {
			edit(selected, false);
		}
	}

	private void onRemove() {
		PrivacyModelConfig selected = list.getSelectedValue();
		if (selected != null) {
			config.getPrivacyModels().remove(selected);
			model.fireUpdate();
		}
	}

	private void edit(PrivacyModelConfig config, boolean isNew) {
		currentConfig = config;
		this.isNew = isNew;

		currentEditor = config.createEditor();

		editComponentContainer.removeAll();
		editComponentContainer.add(currentEditor.getComponent());
		editPanel.updateUI();
		editPanel.setVisible(true);
	}

	private class PrivacyListModel implements ListModel<PrivacyModelConfig> {
		private List<ListDataListener> listeners = new ArrayList<>();

		@Override
		public int getSize() {
			if (config == null) {
				return 0;
			}
			return config.getPrivacyModels().size();
		}

		@Override
		public PrivacyModelConfig getElementAt(int index) {
			return config.getPrivacyModels().get(index);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}

		public void fireUpdate() {
			ListDataEvent evt = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
			for (ListDataListener l : listeners) {
				l.contentsChanged(evt);
			}
		}
	}

}
