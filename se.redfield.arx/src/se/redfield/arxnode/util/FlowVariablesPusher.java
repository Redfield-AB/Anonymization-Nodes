package se.redfield.arxnode.util;

import java.util.function.BiConsumer;

public class FlowVariablesPusher {

	private BiConsumer<String, String> pushString;
	private BiConsumer<String, Double> pushDouble;
	private BiConsumer<String, Integer> pushInt;

	public FlowVariablesPusher(BiConsumer<String, String> pushString, BiConsumer<String, Double> pushDouble,
			BiConsumer<String, Integer> pushInt) {
		this.pushString = pushString;
		this.pushDouble = pushDouble;
		this.pushInt = pushInt;
	}

	public void pushString(String name, String value) {
		pushString.accept(name, value);
	}

	public void pushDouble(String name, Double value) {
		pushDouble.accept(name, value);
	}

	public void pushInt(String name, Integer value) {
		pushInt.accept(name, value);
	}
}
