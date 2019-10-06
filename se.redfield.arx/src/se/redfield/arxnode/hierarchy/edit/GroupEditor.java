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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.deidentifier.arx.aggregates.AggregateFunction;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.hierarchy.edit.HierarchyModelAbstract.HierarchyWizardView;
import se.redfield.arxnode.hierarchy.edit.HierarchyModelGrouping.HierarchyWizardGroupingGroup;

public class GroupEditor<T> extends JPanel implements HierarchyWizardView {
	private static final long serialVersionUID = 1L;

	private HierarchyModelGrouping<T> model;
	private HierarchyWizardGroupingGroup<T> group;

	private AggregateFunctionEditor<T> funcEditor;
	private JSpinner sizeInput;

	private boolean ignoreListeners = false;

	public GroupEditor(HierarchyModelGrouping<T> model) {
		this.model = model;
		model.register(this);
		initUI();
		update();
	}

	private void initUI() {
		funcEditor = new AggregateFunctionEditor<>(model, false, this::onFunctionSelected);
		sizeInput = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		sizeInput.addChangeListener(e -> {
			if (!ignoreListeners && group != null) {
				group.size = getSizeInputValue();
				model.update();
			}
		});

		setLayout(new FormLayout("120:n, 5:n, f:p:g", "p:n, 5:n, p:n"));
		add(funcEditor, CC.rcw(1, 1, 3));
		add(new JLabel("Size: "), CC.rc(3, 1));
		add(sizeInput, CC.rc(3, 3));
	}

	private void onFunctionSelected(AggregateFunction<T> func) {
		if (group != null) {
			group.function = func;
			model.update();
		}
	}

	private int getSizeInputValue() {
		Object value = sizeInput.getValue();
		if (value != null) {
			return (int) value;
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update() {
		Object selected = model.getSelectedElement();
		if (selected instanceof HierarchyWizardGroupingGroup) {
			group = (HierarchyWizardGroupingGroup<T>) selected;
			funcEditor.setFunction(group.function);

			if (getSizeInputValue() != group.size) {
				sizeInput.setValue(group.size);
			}
		} else {
			group = null;
		}
		boolean enabled = group != null;
		funcEditor.setEditorEnabled(enabled);
		sizeInput.setEnabled(enabled);
	}

}
