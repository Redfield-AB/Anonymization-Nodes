package se.redfield.arxnode.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.knime.core.data.DataValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.port.PortObjectSpec;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.HierarchyBinding;

public class HierarchyBindingPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private HierarchyBinding binding;
	private HierarchyBindingPanelListener listener;
	private DialogComponentColumnNameSelection columnInput;

	public HierarchyBindingPanel(HierarchyBinding binding, HierarchyBindingPanelListener listener) {
		super();
		this.binding = binding;
		this.listener = listener;
		initUI();
	}

	private void initUI() {
		setLayout(new FormLayout("f:p:n, 5:n, r:p:n", "t:p:n"));
		add(createInputPanel(), CC.rc(1, 1));
		add(createButtonsPanel(), CC.rc(1, 3));
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedSoftBevelBorder(),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
	}

	private JPanel createButtonsPanel() {
		JButton bRevome = new JButton("-");
		bRevome.addActionListener(e -> listener.onRemove(this));
		JButton bAdd = new JButton("+");
		bAdd.addActionListener(e -> listener.onAdd(this));

		JPanel panel = new JPanel(new FormLayout("p:n", "p:n, 5:n, p:n, p:g"));
		panel.add(bRevome, CC.rc(1, 1));
		panel.add(bAdd, CC.rc(3, 1));
		return panel;
	}

	@SuppressWarnings("unchecked")
	private JPanel createInputPanel() {
		columnInput = new DialogComponentColumnNameSelection(binding.getColumnSetting(), "Column", 0, DataValue.class);
		DialogComponentFileChooser fileInput = new DialogComponentFileChooser(binding.getFileModel(), "arx", "ahs");
		JPanel panel = new JPanel(new FormLayout("l:p:n", "p:n, 5:n, p:n"));
		panel.add(columnInput.getComponentPanel(), CC.rc(1, 1));
		panel.add(fileInput.getComponentPanel(), CC.rc(3, 1));
		return panel;
	}

	public HierarchyBinding getBinding() {
		return binding;
	}

	public void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		columnInput.loadSettingsFrom(settings, specs);
	}

	public static interface HierarchyBindingPanelListener {
		public void onAdd(HierarchyBindingPanel source);

		public void onRemove(HierarchyBindingPanel source);
	}
}
