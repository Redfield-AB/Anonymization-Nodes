package se.redfield.arxnode;

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

import se.redfield.arxnode.config.Config;

public class ArxNodeNodeModel extends NodeModel {

	private static final NodeLogger logger = NodeLogger.getLogger(ArxNodeNodeModel.class);

	private Config config;
	private Anonymizer anonymizer;

	protected ArxNodeNodeModel() {
		super(1, 3);
		config = new Config();
	}

	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		try {
			return anonymizer.process(inData[0], exec);
			// return Partitioner.test(inData[0], config, exec);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw e;
		}

		// // the data table spec of the single output table,
		// // the table will have three columns:
		// DataColumnSpec[] allColSpecs = new DataColumnSpec[3];
		// allColSpecs[0] =
		// new DataColumnSpecCreator("Column 0", StringCell.TYPE).createSpec();
		// allColSpecs[1] =
		// new DataColumnSpecCreator("Column 1", DoubleCell.TYPE).createSpec();
		// allColSpecs[2] =
		// new DataColumnSpecCreator("Column 2", IntCell.TYPE).createSpec();
		// DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
		// // the execution context will provide us with storage capacity, in this
		// // case a data container to which we will add rows sequentially
		// // Note, this container can also handle arbitrary big data tables, it
		// // will buffer to disc if necessary.
		// BufferedDataContainer container = exec.createDataContainer(outputSpec);
		// // let's add m_count rows to it
		// for (int i = 0; i < m_count.getIntValue(); i++) {
		// RowKey key = new RowKey("Row " + i);
		// // the cells of the current row, the types of the cells must match
		// // the column spec (see above)
		// DataCell[] cells = new DataCell[3];
		// cells[0] = new StringCell("String_" + i);
		// cells[1] = new DoubleCell(0.5 * i);
		// cells[2] = new IntCell(i);
		// DataRow row = new DefaultRow(key, cells);
		// container.addRowToTable(row);
		//
		// // check if the execution monitor was canceled
		// exec.checkCanceled();
		// exec.setProgress(i / (double)m_count.getIntValue(),
		// "Adding row " + i);
		// }
		// // once we are done, we close the container and return its table
		// container.close();
		// BufferedDataTable out = container.getTable();
		// return new BufferedDataTable[]{out};
	}

	@Override
	protected void reset() {
		logger.debug("reset");
		if (anonymizer != null) {
			anonymizer.clear();
		}
	}

	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		logger.debug("configure");

		config.initColumns(inSpecs[0]);
		config.validate();
		anonymizer = new Anonymizer(config);

		return new DataTableSpec[] { anonymizer.createOutDataTableSpec(), anonymizer.createStatsTableSpec(),
				inSpecs[0] };
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

		// TODO load internal data.
		// Everything handed to output ports is loaded automatically (data
		// returned by the execute method, models loaded in loadModelContent,
		// and user settings set through loadSettingsFrom - is all taken care
		// of). Load here only the other internals that need to be restored
		// (e.g. data used by the views).

	}

	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		// TODO save internal models.
		// Everything written to output ports is saved automatically (data
		// returned by the execute method, models saved in the saveModelContent,
		// and user settings saved through saveSettingsTo - is all taken care
		// of). Save here only the other internals that need to be preserved
		// (e.g. data used by the views).

	}

}
