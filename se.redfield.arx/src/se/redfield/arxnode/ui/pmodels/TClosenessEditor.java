package se.redfield.arxnode.ui.pmodels;

import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.pmodels.AbstractPrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.TClosenessConfig;
import se.redfield.arxnode.config.pmodels.TClosenessConfig.TClosenessMeasure;

public class TClosenessEditor extends ColumnPrivacyModelEditor {

	private JPanel panel;
	private JComboBox<TClosenessMeasure> cbMesasure;
	private JSpinner tInput;
	private SettingsModelString hierarchyModel;
	private DialogComponentFileChooser hierarchyInput;

	public TClosenessEditor(TClosenessConfig source, Collection<ColumnConfig> columns) {
		cbMesasure = new JComboBox<>(TClosenessMeasure.values());
		tInput = new JSpinner(new SpinnerNumberModel(source.getT(), 0.001, 1, 0.001));
		hierarchyModel = new SettingsModelString("stub", "");
		hierarchyModel.setStringValue(source.getHierarchy());
		hierarchyInput = new DialogComponentFileChooser(hierarchyModel, "hierarchy", "ahs");

		cbMesasure.addActionListener(e -> updateControlsVisibility());
		cbMesasure.setSelectedItem(source.getMeasure());

		CellConstraints cc = new CellConstraints();
		panel = new JPanel(new FormLayout("p:n, 5:n, p:n", "p:n, 5:n, p:n, 5:n, p:n, 5:n, p:n"));
		panel.add(createColumnInput(source, columns), cc.rcw(1, 1, 3));
		panel.add(new JLabel("Measure: "), cc.rc(3, 1));
		panel.add(cbMesasure, cc.rc(3, 3));
		panel.add(new JLabel("T:"), cc.rc(5, 1));
		panel.add(tInput, cc.rc(5, 3));
		panel.add(hierarchyInput.getComponentPanel(), cc.rcw(7, 1, 3));
	}

	private void updateControlsVisibility() {
		TClosenessMeasure measure = (TClosenessMeasure) cbMesasure.getSelectedItem();
		hierarchyInput.getComponentPanel().setVisible(measure == TClosenessMeasure.HIERARCHICAL);
	}

	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public void readFromComponent(AbstractPrivacyModelConfig target) {
		super.readFromComponent(target);
		TClosenessConfig c = (TClosenessConfig) target;
		c.setMeasure((TClosenessMeasure) cbMesasure.getSelectedItem());
		c.setT((double) tInput.getValue());
		c.setHierarchy(hierarchyModel.getStringValue());
	}
}
