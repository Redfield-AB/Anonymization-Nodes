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
package se.redfield.arxnode.ui.transformation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;

public class TransformationColors {
	private static final Color COLOR_LIGHT_GREEN = new Color(150, 255, 150);
	private static final Color COLOR_LIGHT_RED = new Color(255, 150, 150);

	private static TransformationColors instance;

	private Map<Color, ImageIcon> icons;

	private TransformationColors() {
		icons = new HashMap<>();
	}

	public Color getColor(Anonymity anonymity, boolean optimum) {
		switch (anonymity) {
		case ANONYMOUS:
			return optimum ? Color.YELLOW : Color.GREEN;
		case NOT_ANONYMOUS:
			return Color.RED;
		case PROBABLY_ANONYMOUS:
			return COLOR_LIGHT_GREEN;
		case PROBABLY_NOT_ANONYMOUS:
			return COLOR_LIGHT_RED;
		case UNKNOWN:
			return Color.GRAY;
		}
		return Color.GRAY;
	}

	public Color getColor(ARXNode node, ARXNode optimum) {
		return getColor(node.getAnonymity(), optimum != null && node.equals(optimum));
	}

	public ImageIcon getIcon(ARXNode node, ARXNode optimum) {
		Color color = getColor(node, optimum);
		if (!icons.containsKey(color)) {
			icons.put(color, createIcon(color));
		}
		return icons.get(color);
	}

	private ImageIcon createIcon(Color color) {
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setPaint(color);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		return new ImageIcon(image);
	}

	public static TransformationColors getInstance() {
		if (instance == null) {
			instance = new TransformationColors();
		}
		return instance;
	}

	public static Color colorFor(ARXNode node, ARXNode optimum) {
		return getInstance().getColor(node, optimum);
	}

	public static ImageIcon iconFor(ARXNode node, ARXNode optimum) {
		return getInstance().getIcon(node, optimum);
	}
}
