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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.hierarchy.edit.HierarchyModelGrouping.HierarchyWizardGroupingRange;

public class RangeEditor<T> extends JPanel {
	private static final long serialVersionUID = 1L;

	private HierarchyModelGrouping<T> model;
	private HierarchyWizardGroupingRange<T> range;

	public RangeEditor(HierarchyModelGrouping<T> model, HierarchyWizardGroupingRange<T> range, boolean lower) {
		this.model = model;
		this.range = range;
		initUI(lower);
	}

	private void initUI(boolean lower) {
		DataTypedTextEditor<T> snapEditor = new DataTypedTextEditor<>(model, range.snapBound, val -> {
			range.snapBound = val;
		});
		DataTypedTextEditor<T> codingEditor = new DataTypedTextEditor<>(model, range.bottomTopCodingBound, val -> {
			range.bottomTopCodingBound = val;
		});
		DataTypedTextEditor<T> minMaxEditor = new DataTypedTextEditor<>(model, range.minMaxBound, val -> {
			range.minMaxBound = val;
		});

		int snapRow = 1;
		int codingRow = 3;
		int minMaxRow = 5;
		if (lower) {
			minMaxRow = 1;
			codingRow = 3;
			snapRow = 5;
		}
		JLabel lCoding = new JLabel((lower ? "Bottom " : "Top ") + "coding from:");
		JLabel lMinMax = new JLabel((lower ? "Minimum " : "Maximum ") + "value:");

		setLayout(new FormLayout("p:n, 5:n, f:p:g", "p:n, 5:n, p:n, 5:n, p:n"));
		add(new JLabel("Snap from: "), CC.rc(snapRow, 1));
		add(snapEditor.getTextfield(), CC.rc(snapRow, 3));
		add(lCoding, CC.rc(codingRow, 1));
		add(codingEditor.getTextfield(), CC.rc(codingRow, 3));
		add(lMinMax, CC.rc(minMaxRow, 1));
		add(minMaxEditor.getTextfield(), CC.rc(minMaxRow, 3));
		setBorder(BorderFactory.createTitledBorder((lower ? "Lower" : "Upper") + " bound"));
	}

}
