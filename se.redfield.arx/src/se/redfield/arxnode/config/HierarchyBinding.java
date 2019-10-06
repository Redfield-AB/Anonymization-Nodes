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
package se.redfield.arxnode.config;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.FileUtil;

public class HierarchyBinding implements SettingsModelConfig {

	private static final String CONFIG_COLUMN = "column";
	private static final String CONFIG_FILE = "file";

	private SettingsModelColumnName column;
	private SettingsModelString fileModel;

	public HierarchyBinding() {
		column = new SettingsModelColumnName(CONFIG_COLUMN, "");
		fileModel = new SettingsModelString(CONFIG_FILE, "");
	}

	public SettingsModelColumnName getColumnSetting() {
		return column;
	}

	public String getColumnName() {
		return column.getColumnName();
	}

	public SettingsModelString getFileModel() {
		return fileModel;
	}

	public File getFile() throws MalformedURLException {
		return FileUtil.getFileFromURL(FileUtil.toURL(fileModel.getStringValue()));
	}

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(column, fileModel);
	}

	@Override
	public String getKey() {
		return getColumnName();
	}

	@Override
	public void validate() throws InvalidSettingsException {
		SettingsModelConfig.super.validate();
		if (StringUtils.isEmpty(column.getStringValue())) {
			throw new InvalidSettingsException("Target column is not selected");
		}
	}
}
