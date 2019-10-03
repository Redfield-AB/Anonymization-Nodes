package se.redfield.arxnode.ui.transformation;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.criteria.KAnonymity;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.anonymize.AnonymizationResult;
import se.redfield.arxnode.partiton.PartitionInfo;

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

	public static void main(String[] args) throws IOException {
		ARXResult res = getTestResult();
		printLevels(res);

		TransformationSelector filter = new TransformationSelector();
		filter.setModel(new AnonymizationResult(res, new PartitionInfo()));

		JFrame f = new JFrame();
		f.getContentPane().add(filter);
		f.pack();
		f.setLocationByPlatform(true);
		f.setSize(new Dimension(600, 600));
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private static void printLevels(ARXResult res) {
		for (ARXNode[] level : res.getLattice().getLevels()) {
			for (ARXNode n : level) {
				System.out.println(Arrays.toString(n.getTransformation()));
			}
		}
	}

	private static ARXResult getTestResult() throws IOException {
		// Define data
		DefaultData data = Data.create();
		data.add("age", "gender", "zipcode");
		data.add("34", "male", "81667");
		data.add("45", "female", "81675");
		data.add("66", "male", "81925");
		data.add("70", "female", "81931");
		data.add("34", "female", "81931");
		data.add("70", "male", "81931");
		data.add("45", "male", "81931");

		// Define hierarchies
		DefaultHierarchy age = Hierarchy.create();
		age.add("34", "<50", "*");
		age.add("45", "<50", "*");
		age.add("66", ">=50", "*");
		age.add("70", ">=50", "*");

		DefaultHierarchy gender = Hierarchy.create();
		gender.add("male", "*");
		gender.add("female", "*");

		// Only excerpts for readability
		DefaultHierarchy zipcode = Hierarchy.create();
		zipcode.add("81667", "8166*", "816**", "81***", "8****", "*****");
		zipcode.add("81675", "8167*", "816**", "81***", "8****", "*****");
		zipcode.add("81925", "8192*", "819**", "81***", "8****", "*****");
		zipcode.add("81931", "8193*", "819**", "81***", "8****", "*****");

		data.getDefinition().setAttributeType("age", age);
		data.getDefinition().setAttributeType("gender", gender);
		data.getDefinition().setAttributeType("zipcode", zipcode);

		// Create an instance of the anonymizer
		ARXAnonymizer anonymizer = new ARXAnonymizer();
		ARXConfiguration config = ARXConfiguration.create();
		config.addPrivacyModel(new KAnonymity(3));
		config.setSuppressionLimit(0d);

		ARXResult result = anonymizer.anonymize(data, config);
		return result;
	}
}
