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
package se.redfield.arxnode.config.pmodels;

import java.util.Collection;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.ui.pmodels.DPresenceEditor;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;

public class DPresenceConfig extends AbstractPrivacyModelConfig {
	private static final long serialVersionUID = 1L;
	public static final String CONFIG_D_MIN = "dMin";
	public static final String CONFIG_D_MAX = "dMax";

	private double dMin;
	private double dMax;

	public DPresenceConfig() {
		dMin = 0;
		dMax = 0;
	}

	public double getdMin() {
		return dMin;
	}

	public void setdMin(double dMin) {
		this.dMin = dMin;
	}

	public double getdMax() {
		return dMax;
	}

	public void setdMax(double dMax) {
		this.dMax = dMax;
	}

	@Override
	public PrivacyModelEditor createEditor(Collection<ColumnConfig> columns) {
		return new DPresenceEditor(this);
	}

	@Override
	public PrivacyCriterion createCriterion(Data data, Config config) {
		return new DPresence(dMin, dMax, config.getSubsetConfig().createDataSubset(data));
	}

	@Override
	public String getName() {
		return '\u03B4' + "-Presence";
	}

	@Override
	public String toString() {
		return String.format("(%.3f, %.3f)-Presence", dMin, dMax);
	}

	@Override
	public void save(NodeSettingsWO settings) {
		super.save(settings);
		settings.addDouble(CONFIG_D_MIN, dMin);
		settings.addDouble(CONFIG_D_MAX, dMax);
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		super.load(settings);
		dMin = settings.getDouble(CONFIG_D_MIN);
		dMax = settings.getDouble(CONFIG_D_MAX);
	}
}
