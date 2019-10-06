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
package se.redfield.arxnode.hierarchy.edit;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased.Order;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class HierarchyRedactionBasedEditor<T> extends JPanel {
	private static final long serialVersionUID = 1L;

	private HierarchyModelRedaction<T> model;

	public HierarchyRedactionBasedEditor(HierarchyModelRedaction<T> model) {
		super();
		this.model = model;
		initUI();
	}

	private void initUI() {
		setLayout(new FormLayout("f:p:g", "p:n, 5:n, p:n, 5:n, p:n, 5:n, p:n"));
		add(createAlignmentGroup(), CC.rc(1, 1));
		add(createMaskingGroup(), CC.rc(3, 1));
		add(createCharactersGroup(), CC.rc(5, 1));
		add(createDomainGroup(), CC.rc(7, 1));
	}

	private JPanel createAlignmentGroup() {
		JRadioButton rbLeft = new JRadioButton("Align items to the left");
		rbLeft.setSelected(model.getAlignmentOrder() == Order.LEFT_TO_RIGHT);
		rbLeft.addActionListener(e -> model.setAlignmentOrder(Order.LEFT_TO_RIGHT));
		JRadioButton rbRight = new JRadioButton("Align items to the right");
		rbRight.setSelected(model.getAlignmentOrder() == Order.RIGHT_TO_LEFT);
		rbRight.addActionListener(e -> model.setAlignmentOrder(Order.RIGHT_TO_LEFT));
		ButtonGroup group = new ButtonGroup();
		group.add(rbLeft);
		group.add(rbRight);

		JPanel panel = new JPanel(new FormLayout("p:n", "p:n, 5:n, p:n"));
		panel.add(rbLeft, CC.rc(1, 1));
		panel.add(rbRight, CC.rc(3, 1));
		panel.setBorder(BorderFactory.createTitledBorder("Alignment"));
		return panel;
	}

	private JPanel createMaskingGroup() {
		JRadioButton rbLeft = new JRadioButton("Mask items to the left");
		rbLeft.setSelected(model.getRedactionOrder() == Order.LEFT_TO_RIGHT);
		rbLeft.addActionListener(e -> model.setRedactionOrder(Order.LEFT_TO_RIGHT));
		JRadioButton rbRight = new JRadioButton("Mask items to the right");
		rbRight.setSelected(model.getRedactionOrder() == Order.RIGHT_TO_LEFT);
		rbRight.addActionListener(e -> model.setRedactionOrder(Order.RIGHT_TO_LEFT));
		ButtonGroup group = new ButtonGroup();
		group.add(rbLeft);
		group.add(rbRight);

		JPanel panel = new JPanel(new FormLayout("p:n", "p:n, 5:n, p:n"));
		panel.add(rbLeft, CC.rc(1, 1));
		panel.add(rbRight, CC.rc(3, 1));
		panel.setBorder(BorderFactory.createTitledBorder("Masking"));
		return panel;
	}

	private JPanel createCharactersGroup() {
		JComboBox<CharOpt> cbPadding = new JComboBox<>(CharOpt.values());
		cbPadding.setSelectedItem(CharOpt.byChar(model.getPaddingCharacter()));
		cbPadding.addActionListener(e -> model.setPaddingCharacter(((CharOpt) cbPadding.getSelectedItem()).character));

		JComboBox<CharOpt> cbMasking = new JComboBox<>(CharOpt.maskingOptions());
		cbMasking.setSelectedItem(CharOpt.byChar(model.getRedactionCharacter()));
		cbMasking
				.addActionListener(e -> model.setRedactionCharacter(((CharOpt) cbMasking.getSelectedItem()).character));

		JPanel panel = new JPanel(new FormLayout("p:n, 5:n, f:p:g", "p:n, 5:n, p:n"));
		panel.add(new JLabel("Padding character"), CC.rc(1, 1));
		panel.add(cbPadding, CC.rc(1, 3));
		panel.add(new JLabel("Masking character"), CC.rc(3, 1));
		panel.add(cbMasking, CC.rc(3, 3));
		panel.setBorder(BorderFactory.createTitledBorder("Characters"));
		return panel;
	}

	private JPanel createDomainGroup() {
		JSpinner domainInput = createSpinner(model.getDomainSize(), model::setDomainSize);
		JSpinner alphabedInput = createSpinner(model.getAlphabetSize(), model::setAlphabetSize);
		JSpinner maxCharInput = createSpinner(model.getMaxValueLength(), model::setMaxValueLength);

		JPanel panel = new JPanel(new FormLayout("p:n, 5:n, f:p:g", "p:n, 5:n, p:n, 5:n, p:n"));
		panel.add(new JLabel("Domain size"), CC.rc(1, 1));
		panel.add(domainInput, CC.rc(1, 3));
		panel.add(new JLabel("Alphabet size"), CC.rc(3, 1));
		panel.add(alphabedInput, CC.rc(3, 3));
		panel.add(new JLabel("Max. characters"), CC.rc(5, 1));
		panel.add(maxCharInput, CC.rc(5, 3));
		panel.setBorder(BorderFactory.createTitledBorder("Domain properties"));
		return panel;
	}

	private JSpinner createSpinner(Integer initial, Consumer<Integer> setter) {
		JSpinner input = new JSpinner(
				new SpinnerNumberModel(initial != null ? initial.intValue() : 0, 0, Integer.MAX_VALUE, 1));
		input.addChangeListener(e -> setter.accept((Integer) input.getValue()));
		return input;
	}

	private enum CharOpt {
		SPACE(' ', false), ZERO('0', false), ASTERISK('*', true), X('x', true), SHARP('#', true), MINUS('-', true);

		private char character;
		private boolean masking;

		private CharOpt(char character, boolean masking) {
			this.character = character;
			this.masking = masking;
		}

		@Override
		public String toString() {
			return String.format("(%c)", character);
		}

		public static CharOpt byChar(char c) {
			for (CharOpt opt : values()) {
				if (opt.character == c) {
					return opt;
				}
			}
			return null;
		}

		public static CharOpt[] maskingOptions() {
			return Arrays.stream(values()).filter(e -> e.masking).collect(Collectors.toList())
					.toArray(new CharOpt[] {});
		}
	}
}
