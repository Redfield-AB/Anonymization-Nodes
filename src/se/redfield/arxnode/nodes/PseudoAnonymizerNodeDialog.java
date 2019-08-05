package se.redfield.arxnode.nodes;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.PseudoAnonymizerNodeConfig;
import se.redfield.arxnode.config.PseudoAnonymizerNodeConfig.SaltingMode;

public class PseudoAnonymizerNodeDialog extends NodeDialogPane {
	private static final NodeLogger logger = NodeLogger.getLogger(PseudoAnonymizerNodeDialog.class);

	private PseudoAnonymizerNodeConfig config;

	private DialogComponentColumnFilter columnFilter;
	private DialogComponentColumnNameSelection saltColumnInput;

	public PseudoAnonymizerNodeDialog() {
		config = new PseudoAnonymizerNodeConfig();

		addTab("Settings", createSettingsTab());
	}

	private JPanel createSettingsTab() {
		columnFilter = new DialogComponentColumnFilter(config.getColumnFilter(),
				PseudoAnonymizerNodeModel.PORT_DATA_TABLE, false);
		DialogComponentBoolean debugModeInput = new DialogComponentBoolean(config.getDebugMode(),
				"Debug Mode (Output salted values before hashing instead of hash)");

		JPanel panel = new JPanel(new FormLayout("5:n, f:p:g, 5:n", "5:n, f:p:g, 5:n, p:n, 5:n, p:n, 5:n"));
		panel.add(columnFilter.getComponentPanel(), CC.rc(2, 2));
		panel.add(createSaltingPanel(), CC.rc(4, 2));
		panel.add(debugModeInput.getComponentPanel(), CC.rc(6, 2, "c,l"));
		return panel;
	}

	@SuppressWarnings("unchecked")
	private JPanel createSaltingPanel() {
		ModeRadionButton rbNone = new ModeRadionButton(config.getSaltingModeModel(), SaltingMode.NONE, "None");
		ModeRadionButton rbRandom = new ModeRadionButton(config.getSaltingModeModel(), SaltingMode.RANDOM, "Random");
		ModeRadionButton rbColumn = new ModeRadionButton(config.getSaltingModeModel(), SaltingMode.COLUMN, "Column");

		DialogComponentNumber seedInput = new DialogComponentNumber(config.getRandomSeed(), "Seed:", 1);
		saltColumnInput = new DialogComponentColumnNameSelection(config.getSaltColumn(), "",
				PseudoAnonymizerNodeModel.PORT_DATA_TABLE, DataValue.class);

		JPanel panel = new JPanel(new FormLayout("p:n, 5:n, l:p:n", "p:n, 5:n, p:n, 5:n, p:n"));
		panel.add(rbNone, CC.rc(1, 1));
		panel.add(rbRandom, CC.rc(3, 1));
		panel.add(seedInput.getComponentPanel(), CC.rc(3, 3));
		panel.add(rbColumn, CC.rc(5, 1));
		panel.add(saltColumnInput.getComponentPanel(), CC.rc(5, 3));
		panel.setBorder(BorderFactory.createTitledBorder("Salting"));
		return panel;
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		columnFilter.saveSettingsTo(settings);
		config.save(settings);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		DataTableSpec spec = specs[PseudoAnonymizerNodeModel.PORT_DATA_TABLE];
		if ((spec == null) || (spec.getNumColumns() < 1)) {
			throw new NotConfigurableException("Cannot be configured without input table");
		}

		try {
			config.load(settings);
		} catch (InvalidSettingsException e) {
			logger.warn(e.getMessage(), e);
		}

		columnFilter.loadSettingsFrom(settings, specs);
		saltColumnInput.loadSettingsFrom(settings, specs);
	}

	private class ModeRadionButton extends JRadioButton {
		private static final long serialVersionUID = 1L;

		private SaltingMode mode;

		public ModeRadionButton(SettingsModelString model, SaltingMode mode, String title) {
			super(title);
			this.mode = mode;
			addActionListener(e -> {
				config.setSaltingMode(mode);
			});
			model.addChangeListener(e -> {
				updateSelected();
			});
			updateSelected();
		}

		private void updateSelected() {
			setSelected(mode == config.getSaltingMode());
		}
	}
}
