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

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortType;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.anonymize.Pseudoanonymizer;
import se.redfield.arxnode.config.PseudoAnonymizerNodeConfig;

public class PseudoAnonymizerNodeModel extends NodeModel {
	public static final int PORT_DATA_TABLE = 0;

	private PseudoAnonymizerNodeConfig config;

	protected PseudoAnonymizerNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE }, new PortType[] { BufferedDataTable.TYPE });
		config = new PseudoAnonymizerNodeConfig();
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Node doesn't have any internals
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Node doesn't have any internals
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		config.save(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		PseudoAnonymizerNodeConfig temp = new PseudoAnonymizerNodeConfig();
		temp.load(settings);
		temp.validate();
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		config.load(settings);
	}

	@Override
	protected void reset() {
		// No data to reset
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		Utils.removeMissingColumns(config.getColumnFilter(), inSpecs[PORT_DATA_TABLE]);
		return new DataTableSpec[] {
				new Pseudoanonymizer(config, inSpecs[PORT_DATA_TABLE]).getColumnRearranger().createSpec() };
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		Pseudoanonymizer worker = new Pseudoanonymizer(config, inData[PORT_DATA_TABLE].getDataTableSpec());
		BufferedDataTable resultTable = worker.process(exec, inData[PORT_DATA_TABLE]);
		return new BufferedDataTable[] { resultTable };
	}

}
