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

import org.deidentifier.arx.aggregates.AggregateFunction;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.hierarchy.edit.HierarchyModelAbstract.HierarchyWizardView;
import se.redfield.arxnode.hierarchy.edit.HierarchyModelGrouping.HierarchyWizardGroupingInterval;

public class IntervalEditor<T> extends JPanel implements HierarchyWizardView {
	private static final long serialVersionUID = 1L;

	private HierarchyModelGrouping<T> model;
	private HierarchyWizardGroupingInterval<T> interval;

	private AggregateFunctionEditor<T> funcEditor;
	private DataTypedTextEditor<T> minEditor;
	private DataTypedTextEditor<T> maxEditor;

	public IntervalEditor(HierarchyModelGrouping<T> model) {
		this.model = model;
		model.register(this);
		initUI();
		update();
	}

	private void initUI() {
		funcEditor = new AggregateFunctionEditor<>(model, false, this::onFunctionSelected);
		minEditor = new DataTypedTextEditor<>(model, null, val -> {
			if (interval != null) {
				interval.min = val;
			}
		});
		maxEditor = new DataTypedTextEditor<>(model, null, val -> {
			if (interval != null) {
				interval.max = val;
			}
		});

		setLayout(new FormLayout("120:n, 5:n, f:p:g", "p:n, 5:n, p:n, 5:n, p:n"));
		add(funcEditor, CC.rcw(1, 1, 3));
		add(new JLabel("Min: "), CC.rc(3, 1));
		add(minEditor.getTextfield(), CC.rc(3, 3));
		add(new JLabel("Max: "), CC.rc(5, 1));
		add(maxEditor.getTextfield(), CC.rc(5, 3));
	}

	private void onFunctionSelected(AggregateFunction<T> func) {
		if (interval != null) {
			interval.function = func;
			model.update();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update() {
		Object selected = model.getSelectedElement();
		if (selected instanceof HierarchyWizardGroupingInterval) {
			interval = (HierarchyWizardGroupingInterval<T>) selected;
			funcEditor.setFunction(interval.function);
			minEditor.setValue(interval.min);
			maxEditor.setValue(interval.max);

			funcEditor.setEditorEnabled(true);
			minEditor.getTextfield().setEnabled(model.isFirst(interval));
			maxEditor.getTextfield().setEnabled(model.isLast(interval));
		} else {
			interval = null;
			funcEditor.setEditorEnabled(false);
			minEditor.getTextfield().setEnabled(false);
			maxEditor.getTextfield().setEnabled(false);
		}
	}

}
