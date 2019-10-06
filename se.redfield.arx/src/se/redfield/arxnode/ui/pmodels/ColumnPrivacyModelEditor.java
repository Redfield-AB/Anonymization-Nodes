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
package se.redfield.arxnode.ui.pmodels;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deidentifier.arx.AttributeType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.pmodels.AbstractPrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.ColumnPrivacyModelConfig;

public abstract class ColumnPrivacyModelEditor implements PrivacyModelEditor {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(ColumnPrivacyModelEditor.class);

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

	@Override
	public void validate() throws InvalidSettingsException {
		if (cbColumn.getSelectedItem() == null) {
			throw new InvalidSettingsException("Column is not selected");
		}
	}
}
