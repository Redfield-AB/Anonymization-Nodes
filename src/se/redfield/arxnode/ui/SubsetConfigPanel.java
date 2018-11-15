package se.redfield.arxnode.ui;

import java.util.HashMap;
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
		radionButtons = new HashMap<>();
		JRadioButton bNone = createRadioButton("None", SamplingMode.NONE, mode, group);
		JRadioButton bAll = createRadioButton("All", SamplingMode.ALL, mode, group);
		JRadioButton bRandom = createRadioButton("Random selection", SamplingMode.RANDOM, mode, group);
		JRadioButton bQuery = createRadioButton("Query selection", SamplingMode.QUERY, mode, group);

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

	private JRadioButton createRadioButton(String title, SamplingMode mode, SamplingMode current, ButtonGroup group) {
		JRadioButton button = new JRadioButton(title, mode == current);
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
