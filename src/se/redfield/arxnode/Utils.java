package se.redfield.arxnode;

import java.io.File;
import java.io.IOException;

import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.knime.core.node.NodeLogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {

	private static final NodeLogger logger = NodeLogger.getLogger(Utils.class);

	private static long time = 0;

	public static void time() {
		time = System.currentTimeMillis();
	}

	public static void time(String task) {
		long duration = System.currentTimeMillis() - time;
		logger.debug(task + " " + duration + "ms");
		time();
	}

	public static String toPrettyJson(Object obj) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(obj);
	}

	public static DataType<?> knimeToArxType(org.knime.core.data.DataType type) {
		switch (type.getName()) {
		case "String":
			return DataType.STRING;
		case "Number (integer)":
		case "Number (long)":
			return DataType.INTEGER;
		case "Date and Time":
		case "Local Date Time":
			return DataType.DATE;
		case "Number (double)":
			return DataType.DECIMAL;
		}
		logger.warn("Unknown DataType: " + type.getName());
		return DataType.STRING;
	}

	public static Region regionByName(String name) {
		for (Region region : Region.values()) {
			if (region.getName().equals(name)) {
				return region;
			}
		}
		return Region.NONE;
	}

	public static HierarchyBuilder<?> clone(HierarchyBuilder<?> h) throws IOException {
		File f = File.createTempFile("hierarchy", ".ahs");
		f.deleteOnExit();
		h.save(f);
		return HierarchyBuilder.create(f);
	}
}
