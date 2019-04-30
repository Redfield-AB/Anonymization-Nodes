package se.redfield.arxnode.nodes;

import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.gui.swing.HierarchyGroupBasedEditor;
import org.deidentifier.arx.gui.swing.HierarchyModelAbstract;
import org.deidentifier.arx.gui.swing.HierarchyModelIntervals;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.port.PortObjectSpec;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.config.HierarchyCreateNodeConfig;

public class HierarchyCreateNodeDialog extends NodeDialogPane {
	private static final NodeLogger logger = NodeLogger.getLogger(HierarchyCreateNodeDialog.class);

	private static final String PAGE_SELECT = "PAGE_SELECT";
	private static final String PAGE_EDITOR = "PAGE_EDITOR";

	private HierarchyCreateNodeConfig config;
	private DataTableSpec spec;
	private HierarchyModelAbstract<?> model;

	private JPanel cards;
	private JPanel editorPanel;
	private DialogComponentColumnNameSelection columnInput;

	public HierarchyCreateNodeDialog() {
		config = new HierarchyCreateNodeConfig();

		cards = createCards();
		addTab("Hierarchy", cards);
	}

	private JPanel createCards() {
		JPanel panel = new JPanel(new CardLayout());
		panel.add(createSelectPanel(), PAGE_SELECT);
		panel.add(createEditorPanel(), PAGE_EDITOR);
		return panel;
	}

	@SuppressWarnings("unchecked")
	private JPanel createSelectPanel() {
		columnInput = new DialogComponentColumnNameSelection(config.getColumn(), "Column",
				HierarchyCreateNodeModel.PORT_DATA_TABLE, DoubleValue.class);
		JButton bNext = new JButton("Next>");
		bNext.addActionListener(e -> {
			config.setBuilder(null);
			showEditorPage();
		});

		JPanel panel = new JPanel(new FormLayout("l:p:g, 5:n, r:p:n", "p:n, 5:g, p:n"));
		panel.add(columnInput.getComponentPanel(), CC.rc(1, 1));
		panel.add(bNext, CC.rc(3, 3));
		return panel;
	}

	private void showEditorPage() {
		DataType<?> type = Utils.knimeToArxType(spec.getColumnSpec(config.getColumnName()).getType());
		HierarchyModelIntervals<?> intervals = createModel(type);
		this.model = intervals;

		HierarchyGroupBasedEditor<?> editor = new HierarchyGroupBasedEditor<>(intervals);
		editorPanel.removeAll();
		editorPanel.add(editor, CC.rc(1, 1));

		((CardLayout) cards.getLayout()).show(cards, PAGE_EDITOR);
	}

	@SuppressWarnings("unchecked")
	private <T> HierarchyModelIntervals<T> createModel(DataType<T> type) {
		DataColumnSpec columnSpec = spec.getColumnSpec(config.getColumnName());
		List<String> data = new ArrayList<>();
		if (columnSpec.getDomain().hasLowerBound()) {
			data.add(columnSpec.getDomain().getLowerBound().toString());
		}
		if (columnSpec.getDomain().hasUpperBound()) {
			data.add(columnSpec.getDomain().getUpperBound().toString());
		}
		if (columnSpec.getDomain().hasValues()) {
			data.addAll(columnSpec.getDomain().getValues().stream().map(val -> val.toString())
					.collect(Collectors.toList()));
		}
		HierarchyModelIntervals<T> intervals = new HierarchyModelIntervals<>(type, data.toArray(new String[] {}));
		if (config.getBuilder() != null) {
			intervals.parse((HierarchyBuilder<T>) config.getBuilder());
		}
		return intervals;
	}

	private JPanel createEditorPanel() {
		editorPanel = new JPanel(new FormLayout("f:p:g", "f:p:g"));
		return editorPanel;
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			config.load(settings);
		} catch (InvalidSettingsException e) {
			logger.error(e.getMessage(), e);
		}
		columnInput.loadSettingsFrom(settings, specs);
		spec = (DataTableSpec) specs[HierarchyCreateNodeModel.PORT_DATA_TABLE];

		if (config.getBuilder() != null) {
			showEditorPage();
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		if (model != null) {
			try {
				config.setBuilder(model.getBuilder(true));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		config.save(settings);
	}

}
