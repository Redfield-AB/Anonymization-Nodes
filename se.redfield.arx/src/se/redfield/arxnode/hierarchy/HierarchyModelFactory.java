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
package se.redfield.arxnode.hierarchy;

import java.util.Date;
import java.util.Locale;

import javax.swing.JComponent;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.swing.HierarchyDateBasedEditor;
import org.deidentifier.arx.gui.swing.HierarchyGroupBasedEditor;
import org.deidentifier.arx.gui.swing.HierarchyModelAbstract;
import org.deidentifier.arx.gui.swing.HierarchyModelDate;
import org.deidentifier.arx.gui.swing.HierarchyModelIntervals;
import org.deidentifier.arx.gui.swing.HierarchyModelOrder;
import org.deidentifier.arx.gui.swing.HierarchyModelRedaction;
import org.deidentifier.arx.gui.swing.HierarchyOrderBasedEditor;
import org.deidentifier.arx.gui.swing.HierarchyRedactionBasedEditor;

import se.redfield.arxnode.config.HierarchyTypeOptions;

public abstract class HierarchyModelFactory<T, H extends HierarchyModelAbstract<T>> {

	private H model;
	private JComponent editor;

	protected DataType<T> type;
	protected String[] data;

	private HierarchyModelFactory(DataType<T> type, String[] data) {
		this.type = type;
		this.data = data;
	}

	public H getModel() {
		if (model == null) {
			model = createModel();
		}
		return model;
	}

	public JComponent getEditor() {
		if (editor == null) {
			editor = createEditor();
		}
		return editor;
	}

	public abstract H createModel();

	public abstract JComponent createEditor();

	@SuppressWarnings("unchecked")
	public static HierarchyModelFactory<?, ? extends HierarchyModelAbstract<?>> create(HierarchyTypeOptions type,
			DataType<?> dataType, String[] data) {
		switch (type) {
		case INTERVAL:
			return new HierarchyModelFactoryInterval<>(dataType, data);
		case MASKING:
			return new HierarchyModelFactoryMasking<>(dataType, data);
		case DATE:
			return new HierarchyModelFactoryDate((DataType<Date>) dataType, data);
		case ORDER:
			return new HierarchyModelFactoryOrdering<>(dataType, data);
		}
		return null;
	}

	private static class HierarchyModelFactoryInterval<T> extends HierarchyModelFactory<T, HierarchyModelIntervals<T>> {
		private HierarchyModelFactoryInterval(DataType<T> type, String[] data) {
			super(type, data);
		}

		@Override
		public HierarchyModelIntervals<T> createModel() {
			return new HierarchyModelIntervals<>(type, data);
		}

		@Override
		public JComponent createEditor() {
			return new HierarchyGroupBasedEditor<>(getModel());
		}
	}

	private static class HierarchyModelFactoryMasking<T> extends HierarchyModelFactory<T, HierarchyModelRedaction<T>> {
		private HierarchyModelFactoryMasking(DataType<T> type, String[] data) {
			super(type, data);
		}

		@Override
		public HierarchyModelRedaction<T> createModel() {
			return new HierarchyModelRedaction<>(type, data);
		}

		@Override
		public JComponent createEditor() {
			return new HierarchyRedactionBasedEditor<>(getModel());
		}

	}

	private static class HierarchyModelFactoryOrdering<T> extends HierarchyModelFactory<T, HierarchyModelOrder<T>> {
		private HierarchyModelFactoryOrdering(DataType<T> type, String[] data) {
			super(type, data);
		}

		@Override
		public HierarchyModelOrder<T> createModel() {
			return new HierarchyModelOrder<>(type, Locale.getDefault(), data);
		}

		@Override
		public JComponent createEditor() {
			return new HierarchyOrderBasedEditor<>(getModel());
		}
	}

	private static class HierarchyModelFactoryDate extends HierarchyModelFactory<Date, HierarchyModelDate> {
		private HierarchyModelFactoryDate(DataType<Date> type, String[] data) {
			super(type, data);
		}

		@Override
		public HierarchyModelDate createModel() {
			return new HierarchyModelDate(type, data);
		}

		@Override
		public JComponent createEditor() {
			return new HierarchyDateBasedEditor(getModel());
		}
	}
}
