package se.redfield.arxnode.config;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.FileUtil;

public class HierarchyWriterNodeConfig implements SettingsModelConfig {

	public static final String CONFIG_DIR = "dir";
	public static final String CONFIG_PREFIX = "prefix";
	public static final String CONFIG_OVERWRITE = "overwrite";

	private SettingsModelString dir;
	private SettingsModelString prefix;
	private SettingsModelBoolean overwrite;

	public HierarchyWriterNodeConfig() {
		dir = new SettingsModelString(CONFIG_DIR, "");
		prefix = new SettingsModelString(CONFIG_PREFIX, "");
		overwrite = new SettingsModelBoolean(CONFIG_OVERWRITE, false);
	}

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(dir, prefix, overwrite);
	}

	public SettingsModelString getDir() {
		return dir;
	}

	public SettingsModelString getPrefix() {
		return prefix;
	}

	public SettingsModelBoolean getOverwrite() {
		return overwrite;
	}

	public File getDirFile() throws InvalidPathException, MalformedURLException {
		return FileUtil.getFileFromURL(FileUtil.toURL(dir.getStringValue()));
	}

	@Override
	public void validate() throws InvalidSettingsException {
		SettingsModelConfig.super.validate();
		String path = dir.getStringValue();
		if (StringUtils.isEmpty(path)) {
			throw new InvalidSettingsException("Destination dir is not set");
		}
		try {
			File file = getDirFile();
			if (!file.exists()) {
				throw new InvalidSettingsException("Directory " + file.getAbsolutePath() + " does not exist");
			}
			if (!file.isDirectory()) {
				throw new InvalidSettingsException("Specified path " + file.getAbsolutePath() + " is not a directory");
			}
		} catch (InvalidPathException | MalformedURLException e) {
			throw new InvalidSettingsException(e);
		}
	}

	@Override
	public String getKey() {
		return null;
	}

}
