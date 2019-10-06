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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class HierarchyCreateNodeConfig implements SettingsModelConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyCreateNodeConfig.class);

	public static final String CONFIG_COLUMN = "column";
	public static final String CONFIG_TYPE = "type";
	public static final String CONFIG_MODEL = "model";

	private SettingsModelColumnName column;
	private SettingsModelString typeModel;

	private HierarchyTypeOptions type;
	private HierarchyBuilder<?> builder;

	public HierarchyCreateNodeConfig() {
		column = new SettingsModelColumnName(CONFIG_COLUMN, "");
		typeModel = new SettingsModelString(CONFIG_TYPE, "");
		typeModel.addChangeListener(e -> {
			type = HierarchyTypeOptions.fromName(typeModel.getStringValue());
		});
	}

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(column, typeModel);
	}

	@Override
	public void save(NodeSettingsWO settings) {
		SettingsModelConfig.super.save(settings);
		if (builder != null) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				ObjectOutputStream oos = new ObjectOutputStream(os);
				oos.writeObject(builder);
				settings.addByteArray(CONFIG_MODEL, os.toByteArray());
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		SettingsModelConfig.super.load(settings);
		byte[] bytes = settings.getByteArray(CONFIG_MODEL, null);
		if (bytes != null) {
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			try {
				ObjectInputStream ois = new ObjectInputStream(is);
				builder = (HierarchyBuilder<?>) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public SettingsModelColumnName getColumn() {
		return column;
	}

	public String getColumnName() {
		return column.getStringValue();
	}

	public HierarchyTypeOptions getType() {
		return type;
	}

	public SettingsModelString getTypeModel() {
		return typeModel;
	}

	public void setType(HierarchyTypeOptions type) {
		this.typeModel.setStringValue(type.name());
	}

	public HierarchyBuilder<?> getBuilder() {
		return builder;
	}

	public void setBuilder(HierarchyBuilder<?> model) {
		this.builder = model;
	}

	@Override
	public String getKey() {
		return null;
	}

}
