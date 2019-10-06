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
package se.redfield.arxnode.nodes;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;

import se.redfield.arxnode.config.HierarchyWriterNodeConfig;

public class HierarchyWriterNodeDialog extends DefaultNodeSettingsPane {

	private HierarchyWriterNodeConfig config;

	public HierarchyWriterNodeDialog() {
		config = new HierarchyWriterNodeConfig();
		addDialogComponent(
				new DialogComponentFileChooser(config.getDir(), "hierarchy_dir", JFileChooser.OPEN_DIALOG, true));
		addDialogComponent(new DialogComponentString(config.getPrefix(), "File name prefix"));
		addDialogComponent(new DialogComponentBoolean(config.getOverwrite(), "Overwrite existing files"));
	}

}
