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

public class HierarchyCreateNodeConfig implements SettingsModelConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyCreateNodeConfig.class);

	public static final String CONFIG_COLUMN = "column";
	public static final String CONFIG_MODEL = "model";

	private SettingsModelColumnName column;
	private HierarchyBuilder<?> builder;

	public HierarchyCreateNodeConfig() {
		column = new SettingsModelColumnName(CONFIG_COLUMN, "");
	}

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(column);
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

	public HierarchyBuilder<?> getBuilder() {
		return builder;
	}

	public void setBuilder(HierarchyBuilder<?> model) {
		this.builder = model;
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return null;
	}

}
