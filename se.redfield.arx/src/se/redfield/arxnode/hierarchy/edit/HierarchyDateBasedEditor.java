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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilderDate.Format;
import org.deidentifier.arx.aggregates.HierarchyBuilderDate.Granularity;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class HierarchyDateBasedEditor extends JPanel {
	private static final long serialVersionUID = 1L;

	private HierarchyModelDate model;
	private GranularityListItem selectedGranularity;
	private JTextField tfFormat;
	private boolean ignoreListeners = false;

	public HierarchyDateBasedEditor(HierarchyModelDate model) {
		super();
		this.model = model;
		initUI();
	}

	private void initUI() {
		setLayout(new FormLayout("f:p:g", "p:n, 5:n, f:p:g, 5:n, p:n, 5:n, p:n"));
		add(createTopBottomCoding(), CC.rc(1, 1));
		add(createGranularityTable(), CC.rc(3, 1));
		add(createFormatInput(), CC.rc(5, 1));
		add(createTimeZoneInput(), CC.rc(7, 1));
	}

	private JComponent createTopBottomCoding() {
		JPanel panel = new JPanel(new FormLayout("p:n, 5:n, f:p:g, 5:n, p:n, 5:n, f:p:g", "p:n"));
		panel.add(new JLabel("Bottom coding from:"), CC.rc(1, 1));
		panel.add(createCodingTextfield(model::getBottomCodingBound, model::setBottomCodingBound), CC.rc(1, 3));
		panel.add(new JLabel("Top coding from:"), CC.rc(1, 5));
		panel.add(createCodingTextfield(model::getTopCodingBound, model::setTopCodingBound), CC.rc(1, 7));
		return panel;
	}

	private JTextField createCodingTextfield(Supplier<Date> getter, Consumer<Date> setter) {
		JTextField tf = new JTextField();
		Date init = getter.get();
		if (init != null) {
			tf.setText(model.getDataType().format(init));
		}
		tf.setInputVerifier(new ColoredInputVerifier(tf.getForeground()) {

			@Override
			protected boolean checkString(String str) {
				return model.getDataType().isValid(str);
			}
		});
		tf.getDocument().addDocumentListener(new DocmentChangeListener() {
			protected void onChange(DocumentEvent e) {
				String str = tf.getText();
				if (StringUtils.isEmpty(str)) {
					str = DataType.NULL_VALUE;
				}
				if (model.getDataType().isValid(str)) {
					Date newVal = model.getDataType().parse(str);
					if (ObjectUtils.notEqual(newVal, getter.get())) {
						setter.accept(newVal);
						model.update();
					}
				}
			}
		});
		return tf;
	}

	private JComponent createGranularityTable() {
		JList<GranularityListItem> list = new JList<>(createGranularityItems());
		list.setCellRenderer(new GranularityCellRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int index = list.locationToIndex(e.getPoint());
				GranularityListItem item = list.getModel().getElementAt(index);
				item.setSelected(!item.isSelected());
				onGranularitySelected(item);
				list.repaint();
			}
		});
		JScrollPane sp = new JScrollPane(list);
		sp.setBorder(BorderFactory.createTitledBorder("Granularity"));
		return sp;
	}

	private void onGranularitySelected(GranularityListItem item) {
		selectedGranularity = item;
		if (item.isSelected() && !model.getGranularities().contains(item.getGranularity())) {
			model.getGranularities().add(item.getGranularity());
		} else {
			model.getGranularities().remove(item.getGranularity());
		}
		model.update();

		String format = "";
		if (item.getGranularity().isFormatSupported()) {
			format = model.getFormat().get(item.getGranularity());
			if (format == null) {
				format = item.getGranularity().getDefaultFormat();
			}
		}
		ignoreListeners = true;
		tfFormat.setText(format);
		tfFormat.getInputVerifier().verify(tfFormat);
		ignoreListeners = false;
		tfFormat.setEnabled(item.getGranularity().isFormatSupported());

	}

	private GranularityListItem[] createGranularityItems() {
		GranularityListItem[] items = new GranularityListItem[Granularity.values().length];
		for (int i = 0; i < items.length; i++) {
			Granularity gr = Granularity.values()[i];
			String title = StringUtils.lowerCase(gr.name().replace("_", "/"));
			items[i] = new GranularityListItem(gr, title, model.getGranularities().contains(gr));
		}
		return items;
	}

	private JComponent createFormatInput() {
		tfFormat = new JTextField();
		tfFormat.setInputVerifier(new ColoredInputVerifier(tfFormat.getForeground()) {

			@Override
			protected boolean checkString(String str) {
				return model.getFormat().isValid(str, selectedGranularity.getGranularity().getDefaultFormat());
			}
		});
		tfFormat.getDocument().addDocumentListener(new DocmentChangeListener() {

			@Override
			protected void onChange(DocumentEvent e) {
				if (ignoreListeners || selectedGranularity == null) {
					return;
				}
				Granularity g = selectedGranularity.getGranularity();
				if (model.getFormat().isValid(tfFormat.getText(), g.getDefaultFormat())) {
					Format format = model.getFormat();
					format.set(g, tfFormat.getText());
					model.setFormat(format);
				}
			}
		});

		JPanel panel = new JPanel(new FormLayout("f:p:g", "p:n"));
		panel.add(tfFormat, CC.rc(1, 1));
		panel.setBorder(BorderFactory.createTitledBorder("Format"));
		return panel;
	}

	private JComponent createTimeZoneInput() {
		JComboBox<TimeZoneItem> cb = new JComboBox<>(
				Arrays.stream(TimeZone.getAvailableIDs()).map(id -> new TimeZoneItem(TimeZone.getTimeZone(id)))
						.collect(Collectors.toList()).toArray(new TimeZoneItem[] {}));
		cb.setSelectedItem(new TimeZoneItem(model.getTimeZone()));
		cb.addActionListener(e -> {
			TimeZoneItem item = (TimeZoneItem) cb.getSelectedItem();
			model.setTimeZone(item.getTimezone());
		});

		JPanel panel = new JPanel(new FormLayout("f:p:g", "p:n"));
		panel.add(cb, CC.rc(1, 1));
		panel.setBorder(BorderFactory.createTitledBorder("Time Zone"));
		return panel;
	}

	private class GranularityListItem {
		private Granularity granularity;
		private String title;
		private boolean selected;

		public GranularityListItem(Granularity granularity, String title, boolean selected) {
			this.granularity = granularity;
			this.title = title;
			this.selected = selected;
		}

		public Granularity getGranularity() {
			return granularity;
		}

		public String getTitle() {
			return title;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		public boolean isSelected() {
			return selected;
		}
	}

	private class TimeZoneItem {
		private TimeZone timezone;

		public TimeZoneItem(TimeZone timezone) {
			this.timezone = timezone;
		}

		public TimeZone getTimezone() {
			return timezone;
		}

		@Override
		public int hashCode() {
			return timezone.getID().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TimeZoneItem) {
				return timezone.getID().equals(((TimeZoneItem) obj).getTimezone().getID());
			}
			return false;
		}

		@Override
		public String toString() {
			return timezone.getDisplayName();
		}
	}

	private class GranularityCellRenderer extends JCheckBox implements ListCellRenderer<GranularityListItem> {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<? extends GranularityListItem> list,
				GranularityListItem item, int index, boolean isSelected, boolean hasFocus) {
			setEnabled(list.isEnabled());
			setSelected(item.isSelected());
			setFont(list.getFont());
			setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
			setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
			setText(item.getTitle());
			return this;
		}

	}

	private abstract class ColoredInputVerifier extends InputVerifier {
		private Color foreground;
		private Color invalid = Color.RED;

		public ColoredInputVerifier(Color foreground) {
			this.foreground = foreground;
		}

		@Override
		public boolean shouldYieldFocus(JComponent input) {
			verify(input);
			return true;
		}

		@Override
		public boolean verify(JComponent input) {
			boolean valid = checkString(((JTextField) input).getText());
			input.setForeground(valid ? foreground : invalid);
			return valid;
		}

		protected abstract boolean checkString(String str);
	}

	private abstract class DocmentChangeListener implements DocumentListener {
		@Override
		public void changedUpdate(DocumentEvent e) {
			onChange(e);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			onChange(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			onChange(e);
		}

		protected abstract void onChange(DocumentEvent e);
	}

}
