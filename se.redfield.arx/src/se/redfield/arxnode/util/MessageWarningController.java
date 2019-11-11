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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.NodeModel;

/**
 * Class allows to show multiple node's warning messages by grouping them into
 * single string.
 *
 */
public class MessageWarningController {

	private Set<String> messages;
	private Consumer<String> setMessage;

	/**
	 * @param setMessage setWarningMessage method of the {@link NodeModel}
	 */
	public MessageWarningController(Consumer<String> setMessage) {
		this.setMessage = setMessage;
		this.messages = new HashSet<>();
	}

	/**
	 * Clears the warning messages.
	 */
	public void reset() {
		messages.clear();
	}

	/**
	 * Shows warning messages appending it to existing ones if necessary.
	 * 
	 * @param message Warning message.
	 */
	public void showWarning(String message) {
		if (!messages.contains(message)) {
			messages.add(message);
			String joined = StringUtils.join(messages, ";\n");
			setMessage.accept(joined);
		}
	}
}
