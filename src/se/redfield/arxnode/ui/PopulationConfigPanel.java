package se.redfield.arxnode.ui;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.deidentifier.arx.ARXPopulationModel.Region;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.config.pmodels.PopulationConfig;

public class PopulationConfigPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JComboBox<String> cbRegion;
	private JSpinner populationInput;
	private boolean ignoreListener = false;

	public PopulationConfigPanel(PopulationConfig config) {
		super();

		String[] regions = new String[Region.values().length];
		for (int i = 0; i < regions.length; i++) {
			regions[i] = Region.values()[i].getName();
		}
		cbRegion = new JComboBox<>(regions);
		cbRegion.addActionListener(e -> onRegionSelected());

		populationInput = new JSpinner(new SpinnerNumberModel(Long.valueOf(0), Long.valueOf(0),
				Long.valueOf(Long.MAX_VALUE), Long.valueOf(100000)));
		populationInput.addChangeListener(e -> onPopulationChanged());

		setLayout(new FormLayout("p:n, 5:n, f:p:g", "p:n, 5:n, p:n"));
		CellConstraints cc = new CellConstraints();
		add(new JLabel("Region:"), cc.rc(1, 1));
		add(cbRegion, cc.rc(1, 3));
		add(new JLabel("Population size:"), cc.rc(3, 1));
		add(populationInput, cc.rc(3, 3));
		setBorder(BorderFactory.createTitledBorder("Population"));

		cbRegion.setSelectedItem(config.getRegion());
		if (getSelectedRegion() == Region.NONE) {
			populationInput.setValue(config.getPopulationSize());
		}
	}

	private Region getSelectedRegion() {
		return Utils.regionByName((String) cbRegion.getSelectedItem());
	}

	private void onRegionSelected() {
		if (ignoreListener) {
			return;
		}

		Region region = getSelectedRegion();
		if (region != Region.NONE) {
			ignoreListener = true;
			populationInput.setValue(region.getPopulationSize());
			ignoreListener = false;
		}
	}

	private void onPopulationChanged() {
		if (ignoreListener) {
			return;
		}
		ignoreListener = true;
		cbRegion.setSelectedItem(Region.NONE.getName());
		ignoreListener = false;
	}

	public PopulationConfig getPopulationConfig() {
		PopulationConfig config = new PopulationConfig();
		config.setRegion((String) cbRegion.getSelectedItem());
		config.setPopulationSize((long) populationInput.getValue());
		return config;
	}
}
