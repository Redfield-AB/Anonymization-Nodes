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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Json serialization/deserialization class to work with a classes implementing
 * given interface or extending given class.
 *
 * @param <T> Base class or interface.
 */
public class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

	private static final String CLASSNAME = "CLASSNAME";
	private static final String DATA = "DATA";
	private Map<String, Class<?>> classes;

	/**
	 * @param classes List of classes that would be serialized/deserealized.
	 */
	public InterfaceAdapter(Class<?>... classes) {
		this.classes = new HashMap<>();
		for (int i = 0; i < classes.length; i++) {
			Class<?> c = classes[i];
			this.classes.put(c.getSimpleName(), c);
		}
	}

	public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
		String className = prim.getAsString();
		Class<?> klass = getObjectClass(className);
		return jsonDeserializationContext.deserialize(jsonObject.get(DATA), klass);
	}

	public JsonElement serialize(T jsonElement, Type type, JsonSerializationContext jsonSerializationContext) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(CLASSNAME, jsonElement.getClass().getSimpleName());
		jsonObject.add(DATA, jsonSerializationContext.serialize(jsonElement));
		return jsonObject;
	}

	/**
	 * Finds a class by string representation. String could be a simple or full
	 * qualified class name.
	 * 
	 * @param className Class name.
	 * @return Class.
	 */
	public Class<?> getObjectClass(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			Class<?> result = classes.get(toSimpleName(className));
			if (result == null) {
				throw new JsonParseException("Class " + className + " not found");
			}
			return result;
		}
	}

	/**
	 * Converts full qualified class name into simple name.
	 * 
	 * @param className Class name.
	 * @return Class name.
	 */
	private String toSimpleName(String className) {
		int idx = className.lastIndexOf('.');
		if (idx > -1) {
			return className.substring(idx + 1);
		}
		return className;
	}

}
