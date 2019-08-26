package se.redfield.arxnode.util;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;

public class MessageWarningController {

	private Set<String> messages;
	private Consumer<String> setMessage;

	public MessageWarningController(Consumer<String> setMessage) {
		this.setMessage = setMessage;
		this.messages = new HashSet<>();
	}

	public void reset() {
		messages.clear();
	}

	public void showWarning(String message) {
		if (!messages.contains(message)) {
			messages.add(message);
			String joined = StringUtils.join(messages, ";\n");
			setMessage.accept(joined);
		}
	}
}
