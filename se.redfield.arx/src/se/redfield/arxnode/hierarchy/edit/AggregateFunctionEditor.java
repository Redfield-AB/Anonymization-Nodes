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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.aggregates.AggregateFunction.AggregateFunctionWithParameter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class AggregateFunctionEditor<T> extends JPanel {
	private static final long serialVersionUID = 1L;

	private DataType<T> type;
	private Consumer<AggregateFunction<T>> onSelected;

	private JComboBox<AggregateFunctionItem<T>> cbFuction;
	private JTextField tfParam;

	private AggregateFunctionItem<T> selected;
	private boolean ignoreListeners = false;
	private boolean editorEnabled = true;

	public AggregateFunctionEditor(HierarchyModelGrouping<T> model, boolean general,
			Consumer<AggregateFunction<T>> onSelected) {
		this.type = model.getDataType();
		this.onSelected = onSelected;
		initUI(general);
	}

	@SuppressWarnings("unchecked")
	private void initUI(boolean general) {
		cbFuction = new JComboBox<>(new DefaultComboBoxModel<AggregateFunctionItem<T>>(createFunctions(general)));
		cbFuction.addActionListener(e -> {
			if (ignoreListeners) {
				return;
			}
			selected = (AggregateFunctionItem<T>) cbFuction.getSelectedItem();
			update();
			onSelected.accept(selected.function);
		});

		tfParam = new JTextField();
		tfParam.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				updateFunction();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				updateFunction();
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				updateFunction();
			}

			private void updateFunction() {
				if (ignoreListeners) {
					return;
				}
				AggregateFunctionWithParameter<T> withParam = (AggregateFunctionWithParameter<T>) selected.function;
				String param = tfParam.getText();
				if (withParam.acceptsParameter(param)) {
					onSelected.accept(withParam.newInstance(param));
				}
			}
		});

		setLayout(new FormLayout("120:n, 5:n, f:p:g", "p:n, 5:n, p:n"));
		add(new JLabel("Aggregate Function:"), CC.rc(1, 1));
		add(cbFuction, CC.rc(1, 3));
		add(new JLabel("Function Parameter:"), CC.rc(3, 1));
		add(tfParam, CC.rc(3, 3));
	}

	private Vector<AggregateFunctionItem<T>> createFunctions(boolean general) {
		List<AggregateFunctionItem<T>> items = new ArrayList<>();
		if (!general) {
			items.add(new AggregateFunctionItem<>(AggregateFunction.forType(type).createConstantFunction("")));
		}
		items.add(new AggregateFunctionItem<>(AggregateFunction.forType(type).createBoundsFunction()));
		items.add(new AggregateFunctionItem<>(AggregateFunction.forType(type).createPrefixFunction()));
		items.add(new AggregateFunctionItem<>(AggregateFunction.forType(type).createIntervalFunction(true, false)));
		items.add(new AggregateFunctionItem<>(AggregateFunction.forType(type).createSetFunction()));
		items.add(new AggregateFunctionItem<>(AggregateFunction.forType(type).createSetOfPrefixesFunction(1)));
		items.add(new AggregateFunctionItem<>(AggregateFunction.forType(type).createArithmeticMeanFunction()));
		items.add(new AggregateFunctionItem<>(AggregateFunction.forType(type).createArithmeticMeanOfBoundsFunction()));
		items.add(new AggregateFunctionItem<>(AggregateFunction.forType(type).createGeometricMeanFunction()));
		items.add(new AggregateFunctionItem<>(AggregateFunction.forType(type).createGeometricMeanOfBoundsFunction()));
		return new Vector<>(items);
	}

	private void update() {
		ignoreListeners = true;
		if (selected != null) {
			cbFuction.setSelectedItem(selected);
		}
		cbFuction.setEnabled(editorEnabled);
		tfParam.setEnabled(editorEnabled && selected != null && selected.function.hasParameter());
		if (selected != null && selected.function instanceof AggregateFunctionWithParameter) {
			String param = ((AggregateFunctionWithParameter<T>) selected.function).getParameter();
			if (!tfParam.getText().equals(param)) {
				tfParam.setText(param);
			}
		}
		ignoreListeners = false;
	}

	public void setFunction(AggregateFunction<T> func) {
		selected = new AggregateFunctionItem<>(func);
		update();
	}

	public void setEditorEnabled(boolean enabled) {
		this.editorEnabled = enabled;
		update();
	}

	private class AggregateFunctionItem<U> {
		private AggregateFunction<U> function;

		public AggregateFunctionItem(AggregateFunction<U> function) {
			this.function = function;
		}

		@Override
		public String toString() {
			return function.toLabel();
		}

		@Override
		public int hashCode() {
			return function.toLabel().hashCode();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof AggregateFunctionItem) {
				return function.toLabel().equals(((AggregateFunctionItem<U>) obj).function.toLabel());
			}
			return false;
		}
	}
}
