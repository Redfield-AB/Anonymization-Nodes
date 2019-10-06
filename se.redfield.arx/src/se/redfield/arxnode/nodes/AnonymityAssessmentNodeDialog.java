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

import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.AnonymityAssessmentNodeConfig;
import se.redfield.arxnode.ui.PopulationConfigPanel;

public class AnonymityAssessmentNodeDialog extends NodeDialogPane {
	private static final NodeLogger logger = NodeLogger.getLogger(AnonymityAssessmentNodeDialog.class);

	private AnonymityAssessmentNodeConfig config;

	private DialogComponentColumnFilter columnFilter;
	private PopulationConfigPanel population;

	public AnonymityAssessmentNodeDialog() {
		config = new AnonymityAssessmentNodeConfig();

		addTab("Settings", createSettingsPanel());
	}

	private JPanel createSettingsPanel() {
		columnFilter = new DialogComponentColumnFilter(config.getColumnFilter(),
				AnonymityAssessmentNodeModel.PORT_DATA_TABLE, false);
		columnFilter.setIncludeTitle("Quasi-identifying columns");

		DialogComponentNumber threshold = new DialogComponentNumber(config.getRiskThreshold(),
				"Re-identification Risk Threshold", 0.01);

		population = new PopulationConfigPanel(config.getPopulation());

		JPanel panel = new JPanel(new FormLayout("5:n, f:p:g, 5:n", "5:n, f:p:g, 5:n, p:n, 5:n, p:n"));
		panel.add(columnFilter.getComponentPanel(), CC.rc(2, 2));
		panel.add(threshold.getComponentPanel(), CC.rc(4, 2, "c, l"));
		panel.add(population, CC.rc(6, 2));
		return panel;
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		DataTableSpec spec = specs[AnonymityAssessmentNodeModel.PORT_DATA_TABLE];
		if ((spec == null) || (spec.getNumColumns() < 1)) {
			throw new NotConfigurableException("Cannot be configured without input table");
		}

		try {
			config.load(settings);
		} catch (InvalidSettingsException e) {
			logger.warn(e.getMessage(), e);
		}

		columnFilter.loadSettingsFrom(settings, specs);
		population.loadFromConfig();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		config.save(settings);
	}
}
