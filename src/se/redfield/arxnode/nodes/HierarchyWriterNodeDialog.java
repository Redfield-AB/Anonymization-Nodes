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
