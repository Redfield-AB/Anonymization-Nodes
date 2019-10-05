package se.redfield.arxnode.util;

import java.util.function.BiConsumer;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;

public class FlowVariablesPusher {

	private BiConsumer<String, String> pushString;
	private ObjDoubleConsumer<String> pushDouble;
	private ObjIntConsumer<String> pushInt;

	public FlowVariablesPusher(BiConsumer<String, String> pushString, ObjDoubleConsumer<String> pushDouble,
			ObjIntConsumer<String> pushInt) {
		this.pushString = pushString;
		this.pushDouble = pushDouble;
		this.pushInt = pushInt;
	}

	public void pushString(String name, String value) {
		pushString.accept(name, value);
	}

	public void pushDouble(String name, double value) {
		pushDouble.accept(name, value);
	}

	public void pushInt(String name, int value) {
		pushInt.accept(name, value);
	}
}
