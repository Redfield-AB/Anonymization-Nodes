package se.redfield.arxnode.ui.transformation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.apache.mahout.math.Arrays;
import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.criteria.KAnonymity;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.anonymize.AnonymizationResult;

public class TransformationGraph extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final double INITIAL_NODE_WIDTH = 150.0;
	private static final double INITIAL_NODE_HEIGHT = 45.0;
	private static final double NODE_SCREEN_RATIO = 0.8;
	private static final double ZOOM_STEP = 1.2;

	private static final int ATTRIBUTE_VISIBLE = 6;
	private static final int ATTRIBUTE_RECT = 9;
	private static final int ATTRIBUTE_ACTIVE = 10;

	private TransformationFilter filter;
	private AnonymizationResult result;
	private List<List<ARXNode>> levels;
	private ARXNode selected;

	private double zoomFactor = 1.0;
	private int maxLevelWidth;
	private double nodeWidth;
	private double nodeHeight;

	private Color bgColor;
	private Font font;

	public TransformationGraph(TransformationFilter filter) {
		super();
		bgColor = UIManager.getColor("List.background");
		font = UIManager.getFont("Label.font").deriveFont(12f);

		this.filter = filter;
		this.filter.addChangeListener(e -> onFilterChanged());

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onMouseClicked(e.getX(), e.getY());
			}
		});
		addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				zoom(e.getWheelRotation() > 0);
			}
		});
	}

	private void zoom(boolean in) {
		if (in) {
			zoomFactor *= ZOOM_STEP;
		} else {
			zoomFactor /= ZOOM_STEP;
		}
		revalidate();
		repaint();
	}

	public ARXNode getSelected() {
		return selected;
	}

	private void onMouseClicked(int x, int y) {
		selected = getNodeFromPoint(x, y);
		repaint();
	}

	private ARXNode getNodeFromPoint(int x, int y) {
		for (List<ARXNode> level : levels) {
			for (ARXNode node : level) {
				Rectangle rect = (Rectangle) node.getAttributes().get(ATTRIBUTE_RECT);
				if (rect.contains(x, y)) {
					return node;
				}
			}
		}
		return null;
	}

	public void setResult(AnonymizationResult result) {
		this.result = result;
		processNodes();
		revalidate();
		repaint();
	}

	private void onFilterChanged() {
		if (result != null) {
			processNodes();
			revalidate();
			repaint();
		}
	}

	private void processNodes() {
		levels = new ArrayList<>();
		maxLevelWidth = 0;
		int[] transformation = result.getTransformation();

		for (ARXNode[] srcLevel : result.getArxResult().getLattice().getLevels()) {
			List<ARXNode> level = new ArrayList<>();

			for (ARXNode node : srcLevel) {
				boolean visible = filter.isAllowed(node);

				node.getAttributes().put(ATTRIBUTE_VISIBLE, visible);
				node.getAttributes().put(ATTRIBUTE_ACTIVE, node.getTransformation().equals(transformation));

				if (visible) {
					level.add(node);
				}
			}

			if (!level.isEmpty()) {
				levels.add(level);
				maxLevelWidth = Math.max(maxLevelWidth, level.size());
			}
		}
	}

	private void initDimensions() {
		Dimension screen = getSize();
		double width = INITIAL_NODE_WIDTH * zoomFactor;
		double height = INITIAL_NODE_HEIGHT * zoomFactor;

		nodeWidth = width * NODE_SCREEN_RATIO;
		nodeHeight = height * NODE_SCREEN_RATIO;

		double offsetX = (screen.width - width * maxLevelWidth) / 2d;
		double offsetY = (screen.height - height * levels.size()) / 2d;

		double positionY = levels.size() - 1;
		for (List<ARXNode> level : levels) {
			double centerY = offsetY + (positionY * height) + (height / 2d);
			double positionX = 0;
			for (ARXNode node : level) {
				double offset = (maxLevelWidth * width) - (level.size() * width);
				double centerX = offsetX + (positionX * width) + (width / 2d) + (offset / 2d);

				Rectangle rect = new Rectangle((int) (centerX - nodeWidth / 2), (int) (centerY - nodeHeight / 2),
						(int) nodeWidth, (int) nodeHeight);
				node.getAttributes().put(ATTRIBUTE_RECT, rect);
				positionX++;
			}
			positionY--;
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension((int) (INITIAL_NODE_WIDTH * maxLevelWidth * zoomFactor),
				(int) (INITIAL_NODE_HEIGHT * levels.size() * zoomFactor));
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		initDimensions();
		Dimension size = getSize();
		g2d.setColor(bgColor);
		g2d.fillRect(0, 0, size.width, size.height);

		if (result == null) {
			return;
		}

		drawConnections(g2d);
		drawNodes(g2d);
	}

	private void drawConnections(Graphics2D g2d) {
		g2d.setStroke(new BasicStroke(1));
		g2d.setColor(Color.BLACK);

		Set<ARXNode> complete = new HashSet<>();
		for (List<ARXNode> level : levels) {
			for (ARXNode nodeFrom : level) {
				for (ARXNode nodeTo : nodeFrom.getSuccessors()) {
					boolean visible = (boolean) nodeFrom.getAttributes().get(ATTRIBUTE_VISIBLE);
					if (visible && !complete.contains(nodeTo)) {
						drawEdge(g2d, nodeFrom, nodeTo);
					}
				}
				complete.add(nodeFrom);
			}
		}
	}

	private void drawEdge(Graphics g, ARXNode nodeFrom, ARXNode nodeTo) {
		Rectangle from = (Rectangle) nodeFrom.getAttributes().get(ATTRIBUTE_RECT);
		Rectangle to = (Rectangle) nodeTo.getAttributes().get(ATTRIBUTE_RECT);
		g.drawLine((int) from.getCenterX(), (int) from.getCenterY(), (int) to.getCenterX(), (int) to.getCenterY());
	}

	private void drawNodes(Graphics2D g2d) {
		for (List<ARXNode> level : levels) {
			for (ARXNode node : level) {
				Rectangle rect = (Rectangle) node.getAttributes().get(ATTRIBUTE_RECT);
				boolean checked = node == selected
						|| (boolean) node.getAttributes().getOrDefault(ATTRIBUTE_ACTIVE, false);

				g2d.setColor(TransformationColors.colorFor(node, result.getArxResult().getGlobalOptimum()));
				if (checked) {
					g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
				} else {
					g2d.fillOval(rect.x, rect.y, rect.width, rect.height);
				}

				g2d.setColor(getLineColor(node));
				g2d.setStroke(new BasicStroke(2));
				if (checked) {
					g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
				} else {
					g2d.drawOval(rect.x, rect.y, rect.width, rect.height);
				}

				g2d.setFont(font);
				g2d.setColor(Color.BLACK);
				String title = Arrays.toString(node.getTransformation());
				drawString(g2d, title, rect);
			}
		}
	}

	private void drawString(Graphics2D g2d, String str, Rectangle rect) {
		FontMetrics fm = g2d.getFontMetrics();

		int x = rect.x + (rect.width - fm.stringWidth(str)) / 2;
		int y = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();

		g2d.drawString(str, x, y);
	}

	private Color getLineColor(ARXNode node) {
		return (boolean) node.getAttributes().getOrDefault(ATTRIBUTE_ACTIVE, false) ? Color.BLUE : Color.BLACK;
	}

	public static void main(String[] args) throws IOException {

		JPanel panel = new JPanel(new FormLayout("f:100:g", "f:100:g, p:n"));

		TransformationGraph graph = new TransformationGraph(new TransformationFilter() {
			@Override
			public boolean isAllowed(ARXNode node) {
				return true;
			}
		});
		graph.setResult(new AnonymizationResult(getTestResult(), null));
		JScrollPane scroll = new JScrollPane(graph);
		panel.add(scroll, CC.rc(1, 1));

		JFrame f = new JFrame();
		f.getContentPane().add(panel);
		f.pack();
		f.setLocationByPlatform(true);
		f.setSize(new Dimension(600, 600));
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
