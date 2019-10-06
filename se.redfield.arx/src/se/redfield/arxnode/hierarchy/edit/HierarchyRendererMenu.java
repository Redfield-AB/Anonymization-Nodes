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

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import se.redfield.arxnode.hierarchy.edit.HierarchyModelGrouping.HierarchyWizardGroupingGroup;
import se.redfield.arxnode.hierarchy.edit.HierarchyModelGrouping.HierarchyWizardGroupingInterval;

public class HierarchyRendererMenu<T> extends JPopupMenu {
	private static final long serialVersionUID = 1L;

	private HierarchyModelGrouping<T> model;

	private JMenuItem remove;
	private JMenuItem addBefore;
	private JMenuItem addAfter;
	private JMenuItem mergeDown;
	private JMenuItem mergeUp;
	private JMenuItem addRight;

	public HierarchyRendererMenu(HierarchyModelGrouping<T> model) {
		this.model = model;

		remove = createMenuItem("Remove", e -> {
			model.remove(model.getSelectedElement());
		});
		addBefore = createMenuItem("Add Before", e -> {
			model.addBefore(model.getSelectedElement());
		});
		addAfter = createMenuItem("Add After", e -> {
			model.addAfter(model.getSelectedElement());
		});
		mergeDown = createMenuItem("Merge Down", e -> {
			model.mergeDown(model.getSelectedElement());
		});
		mergeUp = createMenuItem("Merge Up", e -> {
			model.mergeUp(model.getSelectedElement());
		});
		addRight = createMenuItem("Add New Level", e -> {
			model.addRight(model.getSelectedElement());
		});

		add(remove);
		addSeparator();
		add(addBefore);
		add(addAfter);
		addSeparator();
		add(mergeDown);
		add(mergeUp);
		addSeparator();
		add(addRight);
	}

	private JMenuItem createMenuItem(String title, ActionListener action) {
		JMenuItem item = new JMenuItem(title);
		item.addActionListener(action);
		return item;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void show(Component invoker, int x, int y) {
		if (model.getSelectedElement() == null) {
			return;
		}
		Object selected = model.getSelectedElement();
		if (selected instanceof HierarchyWizardGroupingInterval) {
			processEnabledState((HierarchyWizardGroupingInterval<T>) selected);
		} else if (selected instanceof HierarchyWizardGroupingGroup) {
			processEnabledState();
		}
		super.show(invoker, x, y);
	}

	private void processEnabledState(HierarchyWizardGroupingInterval<T> interval) {
		if (model.getIntervals().size() == 1) {
			this.remove.setEnabled(false);
		} else if (model.isFirst(interval) || model.isLast(interval)) {
			this.remove.setEnabled(true);
		} else {
			this.remove.setEnabled(false);
		}

		if (model.isFirst(interval)) {
			this.addBefore.setEnabled(true);
			this.mergeDown.setEnabled(false);
		} else {
			this.addBefore.setEnabled(false);
			this.mergeDown.setEnabled(true);
		}
		if (model.isLast(interval)) {
			this.addAfter.setEnabled(true);
			this.mergeUp.setEnabled(false);
		} else {
			this.addAfter.setEnabled(false);
			this.mergeUp.setEnabled(true);
		}

		this.addRight.setEnabled(true);
	}

	private void processEnabledState() {
		if (model.isShowIntervals()) {
			this.remove.setEnabled(true);
		} else {
			if (model.getModelGroups().size() == 1 && model.getModelGroups().get(0).size() == 1) {
				this.remove.setEnabled(false);
			} else {
				this.remove.setEnabled(true);

			}
		}

		this.addBefore.setEnabled(true);
		this.addAfter.setEnabled(true);
		this.addRight.setEnabled(true);
		this.mergeUp.setEnabled(false);
		this.mergeDown.setEnabled(false);
	}
}
