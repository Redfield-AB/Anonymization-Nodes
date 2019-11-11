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
package se.redfield.arxnode.util;

import javax.swing.JButton;
import javax.swing.JPopupMenu;

/**
 * Button that show a pop-up menu on click.
 *
 */
public class PopupMenuButton extends JButton {
	private static final long serialVersionUID = 1L;

	private JPopupMenu menu;

	/**
	 * @param title Button title.
	 * @param menu  Pop-up menu to show on click.
	 */
	public PopupMenuButton(String title, JPopupMenu menu) {
		super(title);
		this.menu = menu;
		addActionListener(e -> onClick());
	}

	/**
	 * onClick handler.
	 */
	private void onClick() {
		menu.show(this, 0, getHeight());
	}
}
