package se.redfield.arxnode;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.knime.core.data.DataCell;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.LongValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.time.localdate.LocalDateValue;
import org.knime.core.data.time.localdatetime.LocalDateTimeValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.NodeLogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {
	private static final NodeLogger logger = NodeLogger.getLogger(Utils.class);
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String DATE_FORMAT = "yyyy-MM-dd";

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
		if (type.isCompatible(LongValue.class) || type.isCompatible(IntValue.class)) {
			return DataType.INTEGER;
		}
		if (type.isCompatible(DoubleValue.class)) {
			return DataType.DECIMAL;
		}
		if (type.isCompatible(LocalDateTimeValue.class)) {
			return DataType.createDate(DATE_TIME_FORMAT);
		}
		if (type.isCompatible(LocalDateValue.class)) {
			return DataType.createDate(DATE_FORMAT);
		}
		return DataType.STRING;
	}

	public static String toString(DataCell cell) {
		if (!cell.isMissing()) {
			if (cell.getType().isCompatible(LocalDateTimeValue.class)) {
				LocalDateTimeValue val = (LocalDateTimeValue) cell;
				return val.getLocalDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
			}
			if (cell.getType().isCompatible(LocalDateValue.class)) {
				LocalDateValue val = (LocalDateValue) cell;
				return val.getLocalDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
			}
		}
		return cell.toString();
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

	public static void addRow(BufferedDataContainer container, List<DataCell> cells, long index) {
		DefaultRow row = new DefaultRow(RowKey.createRowKey(index), cells);
		container.addRowToTable(row);
	}
}
