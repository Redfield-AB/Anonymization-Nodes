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
import javax.swing.SpinnerNumberModel;

import org.knime.core.node.InvalidSettingsException;

import se.redfield.arxnode.config.pmodels.AbstractPrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.KAnonymityConfig;

public class KAnonymityEditor implements PrivacyModelEditor {

	private JPanel editPanel;
	private JSpinner factorInput;

	public KAnonymityEditor(KAnonymityConfig source) {
		factorInput = new JSpinner(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));

		editPanel = new JPanel();
		editPanel.add(new JLabel("K:"));
		editPanel.add(factorInput);

		factorInput.setValue(source.getFactor());
	}

	@Override
	public JComponent getComponent() {
		return editPanel;
	}

	@Override
	public void readFromComponent(AbstractPrivacyModelConfig target) {
		((KAnonymityConfig) target).setFactor((int) factorInput.getValue());
	}

	@Override
	public void validate() throws InvalidSettingsException {
		// nothing to validate
	}
}
