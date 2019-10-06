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
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import se.redfield.arxnode.config.HierarchyCreateNodeConfig;
import se.redfield.arxnode.hierarchy.HierarchyPreviewBuilder;

public class HierarchyCreateNodeModel extends NodeModel {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyCreateNodeModel.class);

	public static final int PORT_DATA_TABLE = 0;
	public static final int PORT_ARX_OBJECT = 1;

	private HierarchyCreateNodeConfig config;
	private ArxPortObjectSpec outSpec;

	protected HierarchyCreateNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, ArxPortObject.TYPE_OPTIONAL },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, ArxPortObject.TYPE });
		config = new HierarchyCreateNodeConfig();
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
		HierarchyCreateNodeConfig temp = new HierarchyCreateNodeConfig();
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
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		if (config.getBuilder() == null) {
			throw new InvalidSettingsException("Hierarchy is not initialized");
		}
		if ((inSpecs[PORT_DATA_TABLE] == null) || (((DataTableSpec) inSpecs[PORT_DATA_TABLE]).getNumColumns() < 1)) {
			throw new InvalidSettingsException("Input table is missing or empty");
		}

		ArxPortObjectSpec inSpec = (ArxPortObjectSpec) inSpecs[PORT_ARX_OBJECT];
		outSpec = new ArxPortObjectSpec(inSpec);
		outSpec.getHierarchies().add(config.getColumnName());
		return new PortObjectSpec[] { inSpecs[PORT_DATA_TABLE], null, outSpec };
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		if (((BufferedDataTable) inObjects[PORT_DATA_TABLE]).size() <= 0) {
			throw new IllegalStateException("Input table is empty");
		}

		ArxPortObject out = ArxPortObject.create(outSpec, (ArxPortObject) inObjects[PORT_ARX_OBJECT]);
		out.getHierarchies().put(config.getColumnName(), config.getBuilder());

		BufferedDataTable preview = new HierarchyPreviewBuilder().build((BufferedDataTable) inObjects[PORT_DATA_TABLE],
				config.getColumnName(), config.getBuilder(), exec);

		return new PortObject[] { inObjects[PORT_DATA_TABLE], preview, out };
	}

}
