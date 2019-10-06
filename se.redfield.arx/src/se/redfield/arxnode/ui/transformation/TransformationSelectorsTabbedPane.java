/*
 * Copyright (c) 2019 Redfield AB.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *  
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package se.redfield.arxnode.ui.transformation;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.knime.core.node.NodeLogger;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.anonymize.AnonymizationResult;

public class TransformationSelectorsTabbedPane extends JPanel {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(TransformationSelectorsTabbedPane.class);

	private JTabbedPane tabs;
	private JLabel errorLabel;
	private List<TransformationSelector> selectors;

	public TransformationSelectorsTabbedPane() {
		super(new FormLayout("f:p:g", "f:p:g"));

		selectors = new ArrayList<>();

		tabs = new JTabbedPane();
		tabs.setTabPlacement(SwingConstants.LEFT);

		errorLabel = new JLabel("No transformations available. Prease re-execute the node");
		errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
		errorLabel.setVerticalAlignment(SwingConstants.CENTER);
	}

	public void setModel(List<AnonymizationResult> results) {
		if (results == null || results.isEmpty()) {
			setContent(errorLabel);
			return;
		}

		initSelectors(results);
		setContent(selectors.size() > 1 ? initTabPanel() : selectors.get(0));
	}

	private void setContent(Component component) {
		removeAll();
		add(component, CC.rc(1, 1));
		updateUI();
	}

	private void initSelectors(List<AnonymizationResult> results) {
		for (int i = 0; i < results.size(); i++) {
			if (i >= selectors.size()) {
				selectors.add(new TransformationSelector());
			}
			selectors.get(i).setModel(results.get(i));
		}

		while (selectors.size() > results.size()) {
			selectors.remove(selectors.size() - 1);
		}
	}

	private JTabbedPane initTabPanel() {
		int selected = tabs.getSelectedIndex();

		tabs.removeAll();
		for (int i = 0; i < selectors.size(); i++) {
			tabs.addTab(String.valueOf(i), selectors.get(i));
		}

		if (selected > -1) {
			tabs.setSelectedIndex(selected);
		}
		return tabs;
	}

	public TransformationSelector getCurrentSelector() {
		int index = selectors.size() > 1 ? tabs.getSelectedIndex() : 0;
		return selectors.get(index);
	}
}
