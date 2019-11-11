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

import se.redfield.arxnode.config.HierarchyTypeOptions;
import se.redfield.arxnode.hierarchy.edit.HierarchyDateBasedEditor;
import se.redfield.arxnode.hierarchy.edit.HierarchyGroupBasedEditor;
import se.redfield.arxnode.hierarchy.edit.HierarchyModelAbstract;
import se.redfield.arxnode.hierarchy.edit.HierarchyModelDate;
import se.redfield.arxnode.hierarchy.edit.HierarchyModelIntervals;
import se.redfield.arxnode.hierarchy.edit.HierarchyModelOrder;
import se.redfield.arxnode.hierarchy.edit.HierarchyModelRedaction;
import se.redfield.arxnode.hierarchy.edit.HierarchyOrderBasedEditor;
import se.redfield.arxnode.hierarchy.edit.HierarchyRedactionBasedEditor;

/**
 * Factory class for creating hierarchy models alongside with appropriate
 * editors.
 *
 * @param <T> {@link HierarchyModelAbstract} param.
 * @param <H> {@link HierarchyModelAbstract} class.
 */
public abstract class HierarchyModelFactory<T, H extends HierarchyModelAbstract<T>> {

	private H model;
	private JComponent editor;

	protected DataType<T> type;
	protected String[] data;

	private HierarchyModelFactory(DataType<T> type, String[] data) {
		this.type = type;
		this.data = data;
	}

	/**
	 * @return Hierarchy model. New model is created if not initialized yet.
	 */
	public H getModel() {
		if (model == null) {
			model = createModel();
		}
		return model;
	}

	/**
	 * @return Hierarchy editor. New instance is created if necessary.
	 */
	public JComponent getEditor() {
		if (editor == null) {
			editor = createEditor();
		}
		return editor;
	}

	/**
	 * @return New hierarchy model instance
	 */
	public abstract H createModel();

	/**
	 * @return New hierarchy editor instance.
	 */
	public abstract JComponent createEditor();

	/**
	 * Creates factory instance based on provided hierarchy type.
	 * 
	 * @param type     Hierarchy type.
	 * @param dataType Hierarchy data type
	 * @param data     Data from column assigned to hierarchy
	 * @return
	 */
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

	/**
	 * Hierarchy model factory for interval based hierarchy.
	 *
	 * @param <T>
	 */
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

	/**
	 * Hierarchy model factory for masking based hierarchy.
	 *
	 * @param <T>
	 */
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

	/**
	 * Hierarchy model factory for order based hierarchy.
	 *
	 * @param <T>
	 */
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

	/**
	 * Hierarchy model factory for date based hierarchy.
	 *
	 */
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
