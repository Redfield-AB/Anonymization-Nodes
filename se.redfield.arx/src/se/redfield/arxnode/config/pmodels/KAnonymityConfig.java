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
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.ui.pmodels.KAnonymityEditor;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;

public class KAnonymityConfig extends AbstractPrivacyModelConfig {
	private static final long serialVersionUID = 1L;

	public static final String CONFIG_FACTOR = "factor";

	private int factor;

	public KAnonymityConfig() {
		factor = 2;
	}

	public int getFactor() {
		return factor;
	}

	public void setFactor(int factor) {
		this.factor = factor;
	}

	@Override
	public PrivacyModelEditor createEditor(Collection<ColumnConfig> columns) {
		return new KAnonymityEditor(this);
	}

	@Override
	public PrivacyCriterion createCriterion(Data data, Config config) {
		return new KAnonymity(factor);
	}

	@Override
	public String getName() {
		return "k-Anonymity";
	}

	@Override
	public String toString() {
		return factor + "-Anonymity";
	}

	@Override
	public void save(NodeSettingsWO settings) {
		super.save(settings);
		settings.addInt(CONFIG_FACTOR, factor);
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		super.load(settings);
		factor = settings.getInt(CONFIG_FACTOR);
	}
}
