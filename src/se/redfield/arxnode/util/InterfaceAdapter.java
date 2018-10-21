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

public class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

	private static final String CLASSNAME = "CLASSNAME";
	private static final String DATA = "DATA";
	private Map<String, Class<?>> classes;

	public InterfaceAdapter(Class<?>... classes) {
		this.classes = new HashMap<>();
		for (int i = 0; i < classes.length; i++) {
			Class<?> c = classes[i];
			this.classes.put(c.getSimpleName(), c);
		}
	}

	public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
			throws JsonParseException {

		JsonObject jsonObject = jsonElement.getAsJsonObject();
		JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
		String className = prim.getAsString();
		Class<?> klass = getObjectClass(className);
		return jsonDeserializationContext.deserialize(jsonObject.get(DATA), klass);
	}

	public JsonElement serialize(T jsonElement, Type type, JsonSerializationContext jsonSerializationContext) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(CLASSNAME, jsonElement.getClass().getName());
		jsonObject.add(DATA, jsonSerializationContext.serialize(jsonElement));
		return jsonObject;
	}

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

	private String toSimpleName(String className) {
		int idx = className.lastIndexOf(".");
		if (idx > -1) {
			return className.substring(idx + 1);
		}
		return className;
	}

}
