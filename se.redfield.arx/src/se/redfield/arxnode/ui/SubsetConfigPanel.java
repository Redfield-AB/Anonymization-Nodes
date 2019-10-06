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

import java.util.EnumMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.SubsetConfig;
import se.redfield.arxnode.config.SubsetConfig.SamplingMode;

public class SubsetConfigPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private SubsetConfig config;
	private DialogComponentNumber probabilityInput;
	private DialogComponentMultiLineString queryInput;
	private Map<SamplingMode, JRadioButton> radionButtons;

	public SubsetConfigPanel(SubsetConfig config) {
		super();
		this.config = config;

		SamplingMode mode = SamplingMode.fromString(config.getMode().getStringValue());
		ButtonGroup group = new ButtonGroup();
		radionButtons = new EnumMap<>(SamplingMode.class);
		JRadioButton bNone = createRadioButton(SamplingMode.NONE, mode, group);
		JRadioButton bAll = createRadioButton(SamplingMode.ALL, mode, group);
		JRadioButton bRandom = createRadioButton(SamplingMode.RANDOM, mode, group);
		JRadioButton bQuery = createRadioButton(SamplingMode.QUERY, mode, group);

		probabilityInput = new DialogComponentNumber(config.getProbability(), "Probability:", 0.1);
		queryInput = new DialogComponentMultiLineString(config.getQuery(), "Query:");

		setLayout(new FormLayout("p:n, 5:n, p:g", "p:n, 5:n, p:n, 5:n, p:n, 5:n, p:n, 5:n, t:p:g"));
		CellConstraints cc = new CellConstraints();
		add(bNone, cc.rcw(1, 1, 3));
		add(bAll, cc.rcw(3, 1, 3));
		add(bRandom, cc.rc(5, 1));
		add(probabilityInput.getComponentPanel(), cc.rc(5, 3));
		add(bQuery, cc.rcw(7, 1, 3));
		add(queryInput.getComponentPanel(), cc.rcw(9, 1, 3));

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		config.getMode().addChangeListener(e -> {
			onModeSelected(SamplingMode.fromString(config.getMode().getStringValue()));
		});
	}

	private JRadioButton createRadioButton(SamplingMode mode, SamplingMode current, ButtonGroup group) {
		JRadioButton button = new JRadioButton(mode.getTitle(), mode == current);
		button.addActionListener(e -> onModeSelected(mode));
		group.add(button);
		radionButtons.put(mode, button);
		return button;
	}

	private void onModeSelected(SamplingMode mode) {
		this.config.getMode().setStringValue(mode.name());
		probabilityInput.getComponentPanel().setVisible(mode == SamplingMode.RANDOM);
		queryInput.getComponentPanel().setVisible(mode == SamplingMode.QUERY);
		radionButtons.get(mode).setSelected(true);
	}
}
