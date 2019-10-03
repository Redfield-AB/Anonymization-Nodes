package se.redfield.arxnode.ui.transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class TransformationFilterPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private TransformationFilter filter;

	private TransformationFilterTable table;
	private List<AnonymityCheckbox> anonCheckboxes;
	private JSpinner minInput;
	private JSpinner maxInput;

	public TransformationFilterPanel() {
		filter = new TransformationFilter();

		table = new TransformationFilterTable(filter);

		setLayout(new FormLayout("f:p:g", "f:100:g, 5:n, p:n, 5:n, p:n"));
		add(new JScrollPane(table), CC.rc(1, 1));
		add(createAnonymityPanel(), CC.rc(3, 1));
		add(createScorePanel(), CC.rc(5, 1));
		setBorder(BorderFactory.createTitledBorder("Filter"));
	}

	private JPanel createAnonymityPanel() {
		AnonymityCheckbox cbAnonymous = new AnonymityCheckbox("Anonymous", Anonymity.ANONYMOUS,
				Anonymity.PROBABLY_ANONYMOUS);
		AnonymityCheckbox cbNonAnonymous = new AnonymityCheckbox("Non-anonymous", Anonymity.NOT_ANONYMOUS,
				Anonymity.PROBABLY_NOT_ANONYMOUS);
		AnonymityCheckbox cbUnknown = new AnonymityCheckbox("Unknown", Anonymity.UNKNOWN);

		anonCheckboxes = new ArrayList<>(Arrays.asList(cbAnonymous, cbNonAnonymous, cbUnknown));

		JPanel panel = new JPanel(new FormLayout("p:g, p:g, p:g", "p:n"));
		panel.add(cbAnonymous, CC.rc(1, 1));
		panel.add(cbNonAnonymous, CC.rc(1, 2));
		panel.add(cbUnknown, CC.rc(1, 3));
		return panel;
	}

	private JPanel createScorePanel() {
		minInput = new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01));
		maxInput = new JSpinner(new SpinnerNumberModel(1, 0, 1, 0.01));

		minInput.addChangeListener(e -> {
			Double value = (Double) minInput.getValue();
			filter.setMinScore(value);
			((SpinnerNumberModel) maxInput.getModel()).setMinimum(value);
		});
		maxInput.addChangeListener(e -> {
			Double value = (Double) maxInput.getValue();
			filter.setMaxScore(value);
			((SpinnerNumberModel) minInput.getModel()).setMaximum(value);
		});

		JPanel panel = new JPanel(new FormLayout("p:n, 5:n, p:g, 5:n, p:n, 5:n, p:g", "p:n"));
		panel.add(new JLabel("Min:"), CC.rc(1, 1));
		panel.add(minInput, CC.rc(1, 3));
		panel.add(new JLabel("Max:"), CC.rc(1, 5));
		panel.add(maxInput, CC.rc(1, 7));
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Score"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		return panel;
	}

	public void setResult(ARXResult result) {
		filter.setResult(result);
		table.getModel().setResult(result);
		for (AnonymityCheckbox cb : anonCheckboxes) {
			cb.readFromFilter();
		}
		minInput.setValue(filter.getMinScore());
		maxInput.setValue(filter.getMaxScore());
	}

	public TransformationFilter getFilter() {
		return filter;
	}

	private class AnonymityCheckbox extends JCheckBox {
		private static final long serialVersionUID = 1L;

		private Anonymity[] modes;

		public AnonymityCheckbox(String title, Anonymity... modes) {
			super(title);
			this.modes = modes;
			addActionListener(e -> filter.setAnonymity(isSelected(), modes));
		}

		public void readFromFilter() {
			setSelected(filter.getAnonymity().containsAll(Arrays.asList(modes)));
		}
	}
}
