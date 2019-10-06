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
package se.redfield.arxnode.ui;

import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.deidentifier.arx.metric.MetricConfiguration;
import org.deidentifier.arx.metric.MetricDescription;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.MetricConfig;
import se.redfield.arxnode.config.MetricConfig.AggregateFunctionOptions;
import se.redfield.arxnode.config.MetricDescriptionWrap;

public class MetricConfigPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private MetricConfig config;
	private JComboBox<MetricDescriptionWrap> cbMeasure;
	private JCheckBox cbMonotonic;
	private JSpinner gsFactorInput;
	private JCheckBox cbPrecomputation;
	private JSpinner precomputationThresholdInput;
	private JComboBox<AggregateFunctionOptions> cbFunc;

	public MetricConfigPanel(MetricConfig config) {
		super();
		this.config = config;
		initUI();
	}

	private void initUI() {
		MetricConfiguration mc = config.getConfiguration();

		cbMeasure = new JComboBox<>(MetricDescriptionWrap.list().toArray(new MetricDescriptionWrap[] {}));
		cbMeasure.setSelectedItem(config.getMeasure());
		cbMeasure.addActionListener(this::onMeasureSelected);

		cbMonotonic = new JCheckBox("Monotonic");
		cbMonotonic.setSelected(mc.isMonotonic());
		cbMonotonic.addActionListener(e -> mc.setMonotonic(cbMonotonic.isSelected()));

		gsFactorInput = new JSpinner(new SpinnerNumberModel(mc.getGsFactor(), 0, 1, 0.01));
		gsFactorInput.addChangeListener(e -> mc.setGsFactor((double) gsFactorInput.getValue()));

		cbPrecomputation = new JCheckBox("Precomputation enabled");
		cbPrecomputation.setSelected(mc.isPrecomputed());
		cbPrecomputation.addActionListener(this::onPrecomputationChanged);

		precomputationThresholdInput = new JSpinner(
				new SpinnerNumberModel(mc.getPrecomputationThreshold(), 0, 1, 0.01));
		precomputationThresholdInput.addChangeListener(
				e -> mc.setPrecomputationThreshold((double) precomputationThresholdInput.getValue()));

		cbFunc = new JComboBox<>(AggregateFunctionOptions.values());
		cbFunc.setSelectedItem(AggregateFunctionOptions.fromString(mc.getAggregateFunction().toString()));
		cbFunc.addActionListener(
				e -> mc.setAggregateFunction(((AggregateFunctionOptions) cbFunc.getSelectedItem()).getFunc()));

		setLayout(new FormLayout("l:p:n, 5:n, f:p:n, p:g", "p:n, 5:n, p:n, 5:n, p:n, 5:n, p:n, 5:n, p:n, 5:n, p:n"));
		add(new JLabel("Measure:"), CC.rc(1, 1));
		add(cbMeasure, CC.rc(1, 3));
		add(cbMonotonic, CC.rc(3, 3));
		add(new JLabel("Generalization/Suppression Factor:"), CC.rc(5, 1));
		add(gsFactorInput, CC.rc(5, 3));
		add(cbPrecomputation, CC.rc(7, 3));
		add(new JLabel("Precomputation Threshold:"), CC.rc(9, 1));
		add(precomputationThresholdInput, CC.rc(9, 3));
		add(new JLabel("Aggregate Fucntion:"), CC.rc(11, 1));
		add(cbFunc, CC.rc(11, 3));
		setBorder(BorderFactory.createTitledBorder("Utility Measure"));
		updateComponentsEnabled();
	}

	private void onMeasureSelected(ActionEvent e) {
		config.setMeasure((MetricDescriptionWrap) cbMeasure.getSelectedItem());
		updateComponentsEnabled();
	}

	private void onPrecomputationChanged(ActionEvent e) {
		config.getConfiguration().setPrecomputed(cbPrecomputation.isSelected());
		updateComponentsEnabled();
	}

	private void updateComponentsEnabled() {
		MetricDescription desc = config.getMeasure().getDescription();
		cbMonotonic.setEnabled(desc.isMonotonicVariantSupported());
		gsFactorInput.setEnabled(desc.isConfigurableCodingModelSupported());
		cbPrecomputation.setEnabled(desc.isPrecomputationSupported());
		precomputationThresholdInput.setEnabled(cbPrecomputation.isEnabled() && cbPrecomputation.isSelected());
		cbFunc.setEnabled(desc.isAggregateFunctionSupported());
	}

	public void loadFromConfig() {
		cbMeasure.setSelectedItem(config.getMeasure());
		MetricConfiguration mc = config.getConfiguration();
		cbMonotonic.setSelected(mc.isMonotonic());
		gsFactorInput.setValue(mc.getGsFactor());
		cbPrecomputation.setSelected(mc.isPrecomputed());
		precomputationThresholdInput.setValue(mc.getPrecomputationThreshold());
		cbFunc.setSelectedItem(AggregateFunctionOptions.fromString(mc.getAggregateFunction().toString()));
	}
}
