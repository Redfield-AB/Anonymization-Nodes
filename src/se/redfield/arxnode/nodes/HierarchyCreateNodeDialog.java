package se.redfield.arxnode.nodes;

import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.gui.swing.HierarchyModelAbstract;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
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
import se.redfield.arxnode.config.HierarchyTypeOptions;
import se.redfield.arxnode.hierarchy.HierarchyModelFactory;

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
	private Map<HierarchyTypeOptions, JRadioButton> buttons;

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
				HierarchyCreateNodeModel.PORT_DATA_TABLE, DataValue.class);
		JButton bNext = new JButton("Next>");
		bNext.addActionListener(e -> {
			config.setBuilder(null);
			showEditorPage();
		});

		JPanel panel = new JPanel(new FormLayout("l:p:g, 5:n, r:p:n", "p:n, 5:n, p:n, 5:g, p:n"));
		panel.add(columnInput.getComponentPanel(), CC.rc(1, 1));
		panel.add(createTypeSelector(), CC.rc(3, 1));
		panel.add(bNext, CC.rc(5, 3));
		return panel;
	}

	private JPanel createTypeSelector() {
		String rows = StringUtils.repeat("p:n, 5:n, ", HierarchyTypeOptions.values().length - 1) + "p:n";
		JPanel panel = new JPanel(new FormLayout("f:p:g", rows));
		buttons = new HashMap<>();
		ButtonGroup group = new ButtonGroup();
		int i = 1;
		for (HierarchyTypeOptions opt : HierarchyTypeOptions.values()) {
			JRadioButton rb = new JRadioButton(opt.getTitle());
			rb.setSelected(opt == config.getType());
			rb.addActionListener(e -> config.setType(opt));
			group.add(rb);
			buttons.put(opt, rb);
			panel.add(rb, CC.rc(i, 1));
			i += 2;
		}

		config.getColumn().addChangeListener(e -> {
			refreshRadioButtonsEnabled();
		});

		return panel;
	}

	private void refreshRadioButtonsEnabled() {
		if (spec == null || StringUtils.isEmpty(config.getColumnName())) {
			return;
		}

		org.knime.core.data.DataType dataType = spec.getColumnSpec(config.getColumnName()).getType();
		for (HierarchyTypeOptions opt : buttons.keySet()) {
			buttons.get(opt).setEnabled(dataType.isCompatible(opt.getDataClass()));
		}

		JRadioButton firstEnabled = null;
		boolean selected = false;
		for (JRadioButton rb : buttons.values()) {
			if (rb.isEnabled() && firstEnabled == null) {
				firstEnabled = rb;
			}
			if (rb.isSelected() && rb.isEnabled()) {
				selected = true;
			}
		}
		if (!selected) {
			firstEnabled.setSelected(true);
			firstEnabled.doClick();
		}
	}

	private void showEditorPage() {
		DataType<?> type = Utils.knimeToArxType(spec.getColumnSpec(config.getColumnName()).getType());
		HierarchyModelFactory<?, ?> factory = createFactory(type);
		model = factory.getModel();

		editorPanel.removeAll();
		editorPanel.add(factory.getEditor(), CC.rc(1, 1));

		((CardLayout) cards.getLayout()).show(cards, PAGE_EDITOR);
	}

	@SuppressWarnings("unchecked")
	private <T> HierarchyModelFactory<T, ?> createFactory(DataType<T> type) {
		HierarchyModelFactory<T, ?> factory = HierarchyModelFactory.create(config.getType(), type, collectData());
		if (config.getBuilder() != null) {
			factory.getModel().parse((HierarchyBuilder<T>) config.getBuilder());
		}
		return factory;
	}

	private String[] collectData() {
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
		return data.toArray(new String[] {});
	}

	private JPanel createEditorPanel() {
		editorPanel = new JPanel(new FormLayout("f:p:g", "f:p:g"));
		return editorPanel;
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		spec = (DataTableSpec) specs[HierarchyCreateNodeModel.PORT_DATA_TABLE];
		if ((spec == null) || (spec.getNumColumns() < 1)) {
			throw new NotConfigurableException("Cannot be configured without" + " input table");
		}

		try {
			config.load(settings);
		} catch (InvalidSettingsException e) {
			logger.error(e.getMessage(), e);
		}
		columnInput.loadSettingsFrom(settings, specs);

		refreshRadioButtonsEnabled();

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
