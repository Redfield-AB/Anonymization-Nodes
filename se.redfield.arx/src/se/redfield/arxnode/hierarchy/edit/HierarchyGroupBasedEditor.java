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
package se.redfield.arxnode.hierarchy.edit;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.hierarchy.edit.HierarchyModelGrouping.HierarchyWizardGroupingGroup;
import se.redfield.arxnode.hierarchy.edit.HierarchyModelGrouping.HierarchyWizardGroupingInterval;

public class HierarchyGroupBasedEditor<T> extends JPanel {
	private static final long serialVersionUID = 1L;

	private HierarchyModelGrouping<T> model;
	private JTabbedPane tabs;

	public HierarchyGroupBasedEditor(HierarchyModelGrouping<T> model) {
		super();
		this.model = model;
		initUI();
	}

	private void initUI() {
		HierarchyRendererPanel<T> rendererPanel = new HierarchyRendererPanel<>(model);
		rendererPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				changeTabOnSelection();
			}
		});
		setLayout(new FormLayout("f:100:g", "f:100:g, 5:n, p:n"));
		add(new JScrollPane(rendererPanel), CC.rc(1, 1));
		add(createTabbedPane(), CC.rc(3, 1));
	}

	private void changeTabOnSelection() {
		Object selected = model.getSelectedElement();
		if (selected instanceof HierarchyWizardGroupingInterval && model.isShowIntervals()) {
			tabs.setSelectedIndex(2);
		} else if (selected instanceof HierarchyWizardGroupingGroup) {
			int index = model.isShowIntervals() ? 3 : 1;
			tabs.setSelectedIndex(index);
		}
	}

	private JComponent createTabbedPane() {
		tabs = new JTabbedPane();
		tabs.addTab("General", createGeneralTab());
		if (model.isShowIntervals()) {
			tabs.addTab("Range", createRageTab());
			tabs.addTab("Interval", createIntervalTab());
		}
		tabs.addTab("Group", createGroupTab());
		return tabs;
	}

	private JComponent createGeneralTab() {
		AggregateFunctionEditor<T> editor = new AggregateFunctionEditor<>(model, true, f -> {
			model.setDefaultFunction(f);
			model.update();
		});
		editor.setFunction(model.getDefaultFunction());
		return editor;
	}

	private JComponent createRageTab() {
		JPanel panel = new JPanel(new FormLayout("f:p:g, 5:n, f:p:g", "p:n"));
		panel.add(new RangeEditor<>(model, model.getLowerRange(), true), CC.rc(1, 1));
		panel.add(new RangeEditor<>(model, model.getUpperRange(), false), CC.rc(1, 3));
		return panel;
	}

	private JComponent createIntervalTab() {
		return new IntervalEditor<>(model);
	}

	private JComponent createGroupTab() {
		return new GroupEditor<>(model);
	}

}
