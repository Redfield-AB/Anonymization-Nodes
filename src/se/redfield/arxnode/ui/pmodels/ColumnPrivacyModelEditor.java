package se.redfield.arxnode.ui.pmodels;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deidentifier.arx.AttributeType;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.pmodels.AbstractPrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.ColumnPrivacyModelConfig;

public abstract class ColumnPrivacyModelEditor implements PrivacyModelEditor {

	private JComboBox<String> cbColumn;

	protected JComponent createColumnInput(ColumnPrivacyModelConfig source, Collection<ColumnConfig> columns) {
		cbColumn = new JComboBox<>(columns.stream().filter(c -> c.getAttrType() == AttributeType.SENSITIVE_ATTRIBUTE)
				.map(ColumnConfig::getName).collect(Collectors.toList()).toArray(new String[] {}));
		cbColumn.setSelectedItem(source.getColumn());

		if (cbColumn.getSelectedIndex() < 0 && cbColumn.getItemCount() > 0) {
			cbColumn.setSelectedIndex(0);
		}

		CellConstraints cc = new CellConstraints();
		JPanel panel = new JPanel(new FormLayout("p:n, 5:n, f:p:g", "p:n"));
		panel.add(new JLabel("Column:"), cc.rc(1, 1));
		panel.add(cbColumn, cc.rc(1, 3));
		return panel;
	}

	@Override
	public void readFromComponent(AbstractPrivacyModelConfig target) {
		((ColumnPrivacyModelConfig) target).setColumn((String) cbColumn.getSelectedItem());
	}
}
