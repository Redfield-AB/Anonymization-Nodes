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
package se.redfield.arxnode.ui;

import javax.swing.JLabel;

import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.arxnode.nodes.ArxPortObjectSpec;

public class HierarchyOverwriteNoticeLabel extends JLabel {
	private static final long serialVersionUID = 1L;

	private SettingsModelString model;
	private ArxPortObjectSpec arxObject;

	public HierarchyOverwriteNoticeLabel(SettingsModelString model) {
		this.model = model;
		model.addChangeListener(e -> updateNotice());
	}

	private void updateNotice() {
		if (arxObject != null) {
			if (arxObject.getHierarchies().contains(model.getStringValue())) {
				setText("<html><font color='red'>"
						+ "Hierarchy for this column is already present and will be overwriten.</font></html>");
			} else {
				setText("");
			}
		}
	}

	public void setArxObject(ArxPortObjectSpec arxObject) {
		this.arxObject = arxObject;
		updateNotice();
	}
}
