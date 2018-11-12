package se.redfield.arxnode.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

import org.knime.core.node.NodeLogger;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.config.pmodels.DPresenceConfig;
import se.redfield.arxnode.config.pmodels.KAnonymityConfig;
import se.redfield.arxnode.config.pmodels.KMapConfig;
import se.redfield.arxnode.config.pmodels.LDiversityConfig;
import se.redfield.arxnode.config.pmodels.PrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.TClosenessConfig;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;
import se.redfield.arxnode.util.PopupMenuButton;

public class PrivacyModelsPane {
	private static final NodeLogger logger = NodeLogger.getLogger(PrivacyModelsPane.class);

	private Config config;

	private JPanel container;
	private JPanel editPanel;
	private JPanel editComponentContainer;

	private JList<PrivacyModelConfig> list;
	private PrivacyListModel model;

	private JButton bEdit;
	private JButton bRemove;

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
		container.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				cancelEdit();
			}
		});
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
		list.addListSelectionListener(e -> onSelectionChanged());

		JButton bAdd = new PopupMenuButton("Add", createDropdownMenu());

		bEdit = new JButton("Edit");
		bEdit.addActionListener(e -> onEdit());
		bEdit.setEnabled(false);
		bRemove = new JButton("Remove");
		bRemove.addActionListener(e -> onRemove());
		bRemove.setEnabled(false);

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

	private JPopupMenu createDropdownMenu() {
		JPopupMenu menu = new JPopupMenu();
		menu.add(createMenuItem(new KAnonymityConfig()));
		menu.add(createMenuItem(new KMapConfig()));
		menu.add(createMenuItem(new DPresenceConfig()));
		menu.addSeparator();
		menu.add(createMenuItem(new LDiversityConfig()));
		menu.add(createMenuItem(new TClosenessConfig()));
		return menu;
	}

	private JMenuItem createMenuItem(PrivacyModelConfig instance) {
		JMenuItem item = new JMenuItem(instance.getName());
		item.addActionListener(e -> edit(instance, true));
		return item;
	}

	private void onSelectionChanged() {
		boolean enabled = !list.isSelectionEmpty();
		bEdit.setEnabled(enabled);
		bRemove.setEnabled(enabled);
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
			cancelEdit();
			config.getPrivacyModels().remove(selected);
			model.fireUpdate();
			list.clearSelection();
		}
	}

	private void edit(PrivacyModelConfig config, boolean isNew) {
		currentConfig = config;
		this.isNew = isNew;

		if (isNew) {
			list.clearSelection();
		}

		currentEditor = config.createEditor(this.config.getColumns().values());

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
