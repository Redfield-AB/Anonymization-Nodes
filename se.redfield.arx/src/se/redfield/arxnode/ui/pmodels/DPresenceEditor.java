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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.knime.core.node.InvalidSettingsException;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.pmodels.AbstractPrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.DPresenceConfig;

public class DPresenceEditor implements PrivacyModelEditor {

	JPanel panel;
	JSpinner dMinInput;
	JSpinner dMaxInput;

	public DPresenceEditor(DPresenceConfig source) {
		dMinInput = new JSpinner(createSpinnerModel());
		dMinInput.setValue(source.getdMin());
		dMaxInput = new JSpinner(createSpinnerModel());
		dMaxInput.setValue(source.getdMax());

		CellConstraints cc = new CellConstraints();
		panel = new JPanel(new FormLayout("p:n, 5:n, f:70:n, p:g", "p:n, 5:n, p:n"));
		panel.add(new JLabel("Lower"), cc.rc(1, 1));
		panel.add(dMinInput, cc.rc(1, 3));
		panel.add(new JLabel("Upper"), cc.rc(3, 1));
		panel.add(dMaxInput, cc.rc(3, 3));

	}

	private SpinnerModel createSpinnerModel() {
		return new SpinnerNumberModel(0.0, 0.0, 1.0, 0.001);
	}

	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public void readFromComponent(AbstractPrivacyModelConfig target) {
		DPresenceConfig c = (DPresenceConfig) target;
		c.setdMin((double) dMinInput.getValue());
		c.setdMax((double) dMaxInput.getValue());
	}

	@Override
	public void validate() throws InvalidSettingsException {
		// nothing to validate
	}
}
