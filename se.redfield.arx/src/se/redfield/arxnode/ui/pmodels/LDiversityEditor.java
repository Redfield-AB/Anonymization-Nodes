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
package se.redfield.arxnode.ui.pmodels;

import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.knime.core.node.NodeLogger;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.pmodels.AbstractPrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.LDiversityConfig;
import se.redfield.arxnode.config.pmodels.LDiversityVariant;

public class LDiversityEditor extends ColumnPrivacyModelEditor {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(LDiversityEditor.class);

	private JPanel panel;
	private JComboBox<LDiversityVariant> cbVariant;
	private JSpinner intLInput;
	private JSpinner doubleLInput;
	private JSpinner cInput;
	private JLabel cLabel;

	public LDiversityEditor(LDiversityConfig source, Collection<ColumnConfig> columns) {
		cbVariant = new JComboBox<>(LDiversityVariant.values());
		intLInput = new JSpinner(new SpinnerNumberModel(source.getIntL(), 2, Integer.MAX_VALUE, 1));
		doubleLInput = new JSpinner(new SpinnerNumberModel(source.getDoubleL(), 2, Integer.MAX_VALUE, 0.1));
		cInput = new JSpinner(new SpinnerNumberModel(source.getC(), 0, Integer.MAX_VALUE, 0.001));
		cLabel = new JLabel("C:");

		cbVariant.addActionListener(e -> updateInputsVisibility());
		cbVariant.setSelectedItem(source.getVariant());

		CellConstraints cc = new CellConstraints();
		panel = new JPanel(new FormLayout("p:n, 5:n, p:n", "p:n, 5:n, p:n, 5:n, p:n, 5:n, p:n"));
		panel.add(createColumnInput(source, columns), cc.rcw(1, 1, 3));
		panel.add(new JLabel("Variant:"), cc.rc(3, 1));
		panel.add(cbVariant, cc.rc(3, 3));
		panel.add(new JLabel("L:"), cc.rc(5, 1));
		panel.add(intLInput, cc.rc(5, 3));
		panel.add(doubleLInput, cc.rc(5, 3));
		panel.add(cLabel, cc.rc(7, 1));
		panel.add(cInput, cc.rc(7, 3));
	}

	private void updateInputsVisibility() {
		LDiversityVariant variant = (LDiversityVariant) cbVariant.getSelectedItem();

		intLInput.setVisible(variant.isIntParam());
		doubleLInput.setVisible(!variant.isIntParam());
		cInput.setVisible(variant.isHasC());
		cLabel.setVisible(variant.isHasC());
	}

	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public void readFromComponent(AbstractPrivacyModelConfig target) {
		super.readFromComponent(target);
		LDiversityConfig c = (LDiversityConfig) target;
		c.setVariant((LDiversityVariant) cbVariant.getSelectedItem());
		c.setIntL((int) intLInput.getValue());
		c.setDoubleL((double) doubleLInput.getValue());
		c.setC((double) cInput.getValue());
	}

}
