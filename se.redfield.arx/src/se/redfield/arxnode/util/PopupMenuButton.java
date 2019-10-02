package se.redfield.arxnode.util;

import javax.swing.JButton;
import javax.swing.JPopupMenu;

public class PopupMenuButton extends JButton {
	private static final long serialVersionUID = 1L;

	private JPopupMenu menu;

	public PopupMenuButton(String title, JPopupMenu menu) {
		super(title);
		this.menu = menu;
		addActionListener(e -> onClick());
	}

	private void onClick() {
		menu.show(this, 0, getHeight());
	}
}
