package se.redfield.arxnode.ui.transformation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.mahout.math.Arrays;
import org.deidentifier.arx.ARXLattice.ARXNode;

import se.redfield.arxnode.anonymize.AnonymizationResult;
import smile.math.Math;

public class TransformationGraph extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final double INITIAL_NODE_WIDTH = 150.0;
	private static final double INITIAL_NODE_HEIGHT = 45.0;
	private static final double NODE_SCREEN_RATIO = 0.8;
	private static final int MIN_TITLED_HEIGHT = 15;

	private static final int ATTRIBUTE_CENTER = 4;
	private static final int ATTRIBUTE_VISIBLE = 6;
	private static final int ATTRIBUTE_ACTIVE = 10;

	private TransformationFilter filter;
	private AnonymizationResult result;
	private List<List<ARXNode>> levels;
	private ARXNode selected;

	private Dimension screen;
	private int maxLevelWidth;
	private double nodeWidth;
	private double nodeHeight;

	private Color bgColor;
	private Font font;

	public TransformationGraph(TransformationFilter filter) {
		super();
		bgColor = UIManager.getColor("List.background");
		font = UIManager.getFont("Label.font");

		this.filter = filter;
		this.filter.addChangeListener(e -> onFilterChanged());

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onMouseClicked(e.getX(), e.getY());
			}
		});
		addComponentListener(new ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				initDimensions();
				repaint();
			};
		});

		DragMouseListener dragListener = new DragMouseListener();
		addMouseListener(dragListener);
		addMouseMotionListener(dragListener);
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
				Rectangle rect = getNodeRect(node);
				if (rect.contains(x, y)) {
					return node;
				}
			}
		}
		return null;
	}

	private Rectangle getNodeRect(ARXNode node) {
		double[] center = (double[]) node.getAttributes().get(ATTRIBUTE_CENTER);
		return new Rectangle((int) (center[0] - nodeWidth / 2), (int) (center[1] - nodeHeight / 2), (int) nodeWidth,
				(int) nodeHeight);
	}

	public void setResult(AnonymizationResult result) {
		this.result = result;
		processNodes();
		repaint();
	}

	private void onFilterChanged() {
		if (result != null) {
			processNodes();
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

		initDimensions();
	}

	private void initDimensions() {
		screen = getSize();
		double width = INITIAL_NODE_WIDTH;
		double height = INITIAL_NODE_HEIGHT;

		double factor = Math.min(screen.height / (height * levels.size()), screen.width / (width * maxLevelWidth));
		if (factor < 1) {
			width *= factor;
			height *= factor;
		}

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
				node.getAttributes().put(ATTRIBUTE_CENTER, new double[] { centerX, centerY });
				positionX++;
			}
			positionY--;
		}
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
					boolean visible = (boolean) nodeTo.getAttributes().get(ATTRIBUTE_VISIBLE);
					if (visible && !complete.contains(nodeTo)) {
						drawEdge(g2d, nodeFrom, nodeTo);
					}
				}
				complete.add(nodeFrom);
			}
		}
	}

	private void drawEdge(Graphics g, ARXNode nodeFrom, ARXNode nodeTo) {
		double[] from = (double[]) nodeFrom.getAttributes().get(ATTRIBUTE_CENTER);
		double[] to = (double[]) nodeTo.getAttributes().get(ATTRIBUTE_CENTER);
		g.drawLine((int) from[0], (int) from[1], (int) to[0], (int) to[1]);
	}

	private void drawNodes(Graphics2D g2d) {
		for (List<ARXNode> level : levels) {
			for (ARXNode node : level) {
				Rectangle rect = getNodeRect(node);
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

				if (rect.height > MIN_TITLED_HEIGHT) {
					g2d.setFont(font);
					g2d.setColor(Color.BLACK);
					drawString(g2d, getTransformationString(node), rect);
				}
			}
		}
	}

	private String getTransformationString(ARXNode node) {
		String str = Arrays.toString(node.getTransformation());
		return str.substring(1, str.length() - 1);
	}

	private void drawString(Graphics2D g2d, String str, Rectangle rect) {
		FontMetrics fm = g2d.getFontMetrics();

		int x = rect.x + (rect.width - fm.stringWidth(str)) / 2;
		int y = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();

		double factor = Math.min(rect.width * 0.7 / fm.stringWidth(str), rect.height * 0.7 / fm.getHeight());
		double scaledX = rect.x + (rect.width - fm.stringWidth(str) * factor) / 2;
		double scaledY = rect.y + (rect.height - fm.getHeight() * factor) / 2 + fm.getAscent() * factor;

		AffineTransform tr = new AffineTransform();
		tr.translate(scaledX, scaledY);
		tr.scale(factor, factor);
		tr.translate(-x, -y);

		AffineTransform prev = g2d.getTransform();
		g2d.transform(tr);
		g2d.drawString(str, x, y);
		g2d.setTransform(prev);
	}

	private Color getLineColor(ARXNode node) {
		return (boolean) node.getAttributes().getOrDefault(ATTRIBUTE_ACTIVE, false) ? Color.BLUE : Color.BLACK;
	}

	private class DragMouseListener extends MouseAdapter {
		private static final double ZOOM_SPEED = 10.0;
		private Point drag;
		private Point dragStart;
		private DragMode mode = DragMode.NONE;

		@Override
		public void mousePressed(MouseEvent e) {
			drag = e.getPoint();
			dragStart = e.getPoint();
			if (mode == DragMode.NONE) {
				if (SwingUtilities.isRightMouseButton(e)) {
					mode = DragMode.ZOOM;
				} else if (SwingUtilities.isLeftMouseButton(e)) {
					mode = DragMode.PAN;
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			mode = DragMode.NONE;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (mode == DragMode.NONE) {
				return;
			}

			Point pt = e.getPoint();
			Point delta = new Point(pt.x - drag.x, pt.y - drag.y);
			drag = pt;

			if (mode == DragMode.PAN) {
				for (List<ARXNode> level : levels) {
					for (ARXNode node : level) {
						double[] center = (double[]) node.getAttributes().get(ATTRIBUTE_CENTER);
						center[0] += delta.x;
						center[1] += delta.y;
					}
				}
			} else if (mode == DragMode.ZOOM) {
				double zoom = -((double) delta.y / (double) screen.height) * ZOOM_SPEED;
				zoom = trimZoom(zoom, nodeWidth, 1, screen.width);
				zoom = trimZoom(zoom, nodeHeight, 1, screen.height);

				nodeWidth += zoom * nodeWidth;
				nodeHeight += zoom * nodeHeight;

				for (List<ARXNode> level : levels) {
					for (ARXNode node : level) {
						double[] center = (double[]) node.getAttributes().get(ATTRIBUTE_CENTER);
						center[0] = zoomCenter(center[0], zoom, dragStart.x);
						center[1] = zoomCenter(center[1], zoom, dragStart.y);
					}
				}
			}
			repaint();
		}

		private double trimZoom(double zoom, double val, double min, double max) {
			double result = zoom;
			double zoomed = val + zoom * val;
			if (zoomed > max) {
				return (max - val) / val;
			}
			if (zoomed < min) {
				return (min - val) / val;
			}
			return result;
		}

		private double zoomCenter(double val, double zoom, double start) {
			return (val - start) * (1 + zoom) + start;
		}
	}

	private enum DragMode {
		NONE, PAN, ZOOM
	}

}
