package se.redfield.arxnode.hierarchy;

import javax.swing.JComponent;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.swing.HierarchyGroupBasedEditor;
import org.deidentifier.arx.gui.swing.HierarchyModelAbstract;
import org.deidentifier.arx.gui.swing.HierarchyModelIntervals;
import org.deidentifier.arx.gui.swing.HierarchyModelRedaction;
import org.deidentifier.arx.gui.swing.HierarchyRedactionBasedEditor;

import se.redfield.arxnode.config.HierarchyTypeOptions;

public abstract class HierarchyModelFactory<T, HM extends HierarchyModelAbstract<T>> {

	private HM model;
	private JComponent editor;

	protected DataType<T> type;
	protected String[] data;

	private HierarchyModelFactory(DataType<T> type, String[] data) {
		this.type = type;
		this.data = data;
	}

	public HM getModel() {
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

	public abstract HM createModel();

	public abstract JComponent createEditor();

	public static <T> HierarchyModelFactory<T, ? extends HierarchyModelAbstract<T>> create(HierarchyTypeOptions type,
			DataType<T> dataType, String[] data) {
		switch (type) {
		case INTERVAL:
			return new HierarchyModelFactoryInterval<T>(dataType, data);
		case MASKING:
			return new HierarchyModelFactoryMasking<T>(dataType, data);
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
}
