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
