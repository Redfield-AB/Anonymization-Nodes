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
package se.redfield.arxnode.hierarchy.edit;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import se.redfield.arxnode.hierarchy.edit.HierarchyModelAbstract.HierarchyWizardView;
import se.redfield.arxnode.hierarchy.edit.HierarchyRenderer.RenderedComponent;
import se.redfield.arxnode.hierarchy.edit.HierarchyRenderer.RenderedGroup;
import se.redfield.arxnode.hierarchy.edit.HierarchyRenderer.RenderedInterval;

public class HierarchyRendererPanel<T> extends JPanel implements HierarchyWizardView {
	private static final long serialVersionUID = 1L;

	private HierarchyModelGrouping<T> model;

	public HierarchyRendererPanel(HierarchyModelGrouping<T> model) {
		this.model = model;
		model.register(this);
		JPopupMenu menu = new HierarchyRendererMenu<>(model);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (model.getRenderer().select(e.getX(), e.getY())) {
					model.updateUI(HierarchyRendererPanel.this);
					repaint();
				}

				if (SwingUtilities.isRightMouseButton(e)) {
					menu.show(HierarchyRendererPanel.this, e.getX(), e.getY());
				}
			}
		});
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		model.getRenderer().update(g);

		g.setColor(HierarchyRenderer.WIDGET_BACKGROUND);
		g.fillRect(0, 0, getWidth(), getHeight());

		for (RenderedComponent<T> component : model.getRenderer().getComponents()) {
			Color foreground = HierarchyRenderer.NORMAL_FOREGROUND;
			Color alternative = HierarchyRenderer.ALTERNATIVE_FOREGROUND;
			Color background = HierarchyRenderer.NORMAL_BACKGROUND;

			if (isSelected(component)) {
				background = HierarchyRenderer.SELECTED_BACKGROUND;
				alternative = background;
			}

			if (!component.enabled) {
				foreground = HierarchyRenderer.DISABLED_FOREGROUND;
				background = HierarchyRenderer.DISABLED_BACKGROUND;
				alternative = background;
			}

			drawComponentRect(g, background, foreground, component.rectangle1, component.bounds);
			drawComponentRect(g, foreground, alternative, component.rectangle2, component.label);
		}
		if (!getPreferredSize().equals(model.getRenderer().getMinSize())) {
			setPreferredSize(model.getRenderer().getMinSize());
			revalidate();
		}
	}

	private void drawComponentRect(Graphics g, Color background, Color foreground, Rectangle rect, String label) {
		g.setColor(background);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
		g.setColor(foreground);
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
		drawString(g, label, rect);
	}

	private void drawString(Graphics g, String str, Rectangle rect) {
		g.setFont(HierarchyRenderer.FONT);
		Rectangle2D bounds = g.getFontMetrics().getStringBounds(str, g);
		FontMetrics metrics = g.getFontMetrics();

		int x = rect.x + (rect.width - (int) bounds.getWidth()) / 2;
		int y = rect.y + metrics.getAscent() + (rect.height - metrics.getHeight()) / 2;
		g.drawString(str, x, y);
	}

	private boolean isSelected(RenderedComponent<T> component) {
		if (model.getSelectedElement() == null)
			return false;
		if (component instanceof RenderedInterval) {
			return ((RenderedInterval<T>) component).interval.equals(model.getSelectedElement());
		} else {
			return ((RenderedGroup<T>) component).group.equals(model.getSelectedElement());
		}
	}

	@Override
	public void update() {
		repaint();
	}

}
