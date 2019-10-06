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

import java.util.function.Consumer;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.deidentifier.arx.DataType;

public class DataTypedTextEditor<T> implements DocumentListener {

	private HierarchyModelGrouping<T> model;
	private DataType<T> type;
	private Consumer<T> valueChanged;
	private JTextField textfield;

	private boolean ignoreChange = false;

	public DataTypedTextEditor(HierarchyModelGrouping<T> model, T initialVal, Consumer<T> valueChanged) {
		this.model = model;
		this.type = model.getDataType();
		this.valueChanged = valueChanged;
		textfield = new JTextField();
		textfield.getDocument().addDocumentListener(this);
		if (initialVal != null) {
			setValue(initialVal);
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		onChange();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		onChange();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		onChange();
	}

	private void onChange() {
		if (ignoreChange) {
			return;
		}
		T newVal = parseValue();
		if (newVal != null) {
			valueChanged.accept(newVal);
			model.update();
		}
	}

	private T parseValue() {
		String val = textfield.getText();
		if (type.isValid(val)) {
			return type.parse(val);
		}
		return null;
	}

	public JTextField getTextfield() {
		return textfield;
	}

	public void setValue(T val) {
		ignoreChange = true;
		T prev = parseValue();
		if (type.compare(val, prev) != 0) {
			textfield.setText(type.format(val));
		}
		ignoreChange = false;
	}
}
