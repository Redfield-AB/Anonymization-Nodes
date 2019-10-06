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
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import org.knime.core.node.interactive.InteractiveNode;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.web.ValidationError;

import se.redfield.arxnode.anonymize.AnonymizationResult;
import se.redfield.arxnode.anonymize.AnonymizationResultProcessor;
import se.redfield.arxnode.anonymize.Anonymizer;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.nodes.AnonymizerNodeView.AnonymizerNodeViewValue;
import se.redfield.arxnode.util.FlowVariablesPusher;
import se.redfield.arxnode.util.MessageWarningController;

public class AnonymizerNodeModel extends NodeModel
		implements InteractiveNode<AnonymizerNodeViewValue, AnonymizerNodeViewValue> {

	private static final NodeLogger logger = NodeLogger.getLogger(AnonymizerNodeModel.class);

	public static final int PORT_DATA_TABLE = 0;
	public static final int PORT_ARX = 1;

	private Config config;
	private AnonymizationResultProcessor outputBuilder;
	private MessageWarningController warnings;
	private FlowVariablesPusher fwPusher;
	private List<AnonymizationResult> results;
	private AnonymizerNodeViewValue viewValue;

	protected AnonymizerNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, ArxPortObject.TYPE_OPTIONAL },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE,
						BufferedDataTable.TYPE, FlowVariablePortObject.TYPE });
		config = new Config();
		warnings = new MessageWarningController(this::setWarningMessage);
		fwPusher = new FlowVariablesPusher(this::pushFlowVariableString, this::pushFlowVariableDouble,
				this::pushFlowVariableInt);
	}

	public List<AnonymizationResult> getResults() {
		return results;
	}

	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
		logger.debug("execute");
		try {
			warnings.reset();
			if (results == null) {
				logger.debug("anonymizing");
				Anonymizer anonymizer = new Anonymizer(config);
				results = anonymizer.process((BufferedDataTable) inData[PORT_DATA_TABLE],
						(ArxPortObject) inData[PORT_ARX]);
			}
			logger.debug("processing result");

			if (viewValue != null && StringUtils.isNotEmpty(viewValue.getWarning())) {
				warnings.showWarning(viewValue.getWarning());
			}

			return outputBuilder.process((BufferedDataTable) inData[PORT_DATA_TABLE], results, exec);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void reset() {
		logger.debug("reset");
		warnings.reset();
		results = null;
	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		logger.debug("configure");
		if ((inSpecs[PORT_DATA_TABLE] == null) || (((DataTableSpec) inSpecs[PORT_DATA_TABLE]).getNumColumns() < 1)) {
			throw new InvalidSettingsException("Input table is missing or empty");
		}

		config.configure((DataTableSpec) inSpecs[PORT_DATA_TABLE], (ArxPortObjectSpec) inSpecs[PORT_ARX]);
		config.validate();

		outputBuilder = new AnonymizationResultProcessor(config, warnings, fwPusher);

		return new PortObjectSpec[] { outputBuilder.createOutDataTableSpec(), outputBuilder.createStatsTableSpec(),
				inSpecs[PORT_DATA_TABLE], outputBuilder.createRiskTableSpec(), FlowVariablePortObjectSpec.INSTANCE };
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		logger.debug("saveSettingsTo");
		config.save(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		logger.debug("loadValidatedSettingsFrom");
		config.load(settings);
	}

	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		logger.debug("validateSettings");
		config.validate(settings);
	}

	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Node doesn't have any internals
	}

	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Node doesn't have any internals
	}

	@Override
	public AnonymizerNodeViewValue getViewRepresentation() {
		logger.debug("getViewRepresentation");
		return null;
	}

	@Override
	public AnonymizerNodeViewValue getViewValue() {
		logger.debug("getViewValue");
		return null;
	}

	@Override
	public ValidationError validateViewValue(AnonymizerNodeViewValue viewContent) {
		logger.debug("validateViewValue");
		return null;
	}

	@Override
	public void loadViewValue(AnonymizerNodeViewValue viewContent, boolean useAsDefault) {
		logger.debug("loadViewValue: ");
		this.viewValue = viewContent;
	}
}
