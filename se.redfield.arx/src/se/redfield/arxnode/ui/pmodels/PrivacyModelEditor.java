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

import org.knime.core.node.InvalidSettingsException;

import se.redfield.arxnode.config.pmodels.AbstractPrivacyModelConfig;

public interface PrivacyModelEditor {
	public JComponent getComponent();

	public void readFromComponent(AbstractPrivacyModelConfig target);

	public void validate() throws InvalidSettingsException;
}
