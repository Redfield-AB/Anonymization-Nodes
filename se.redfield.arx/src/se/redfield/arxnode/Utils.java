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
package se.redfield.arxnode;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.LongValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.time.localdate.LocalDateValue;
import org.knime.core.data.time.localdatetime.LocalDateTimeValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Class to hold some utility methods.
 *
 */
public class Utils {
	private static final NodeLogger logger = NodeLogger.getLogger(Utils.class);
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	private static long time = 0;

	private Utils() {
		// empty constructor
	}

	public static void time() {
		time = System.currentTimeMillis();
	}

	public static void time(String task) {
		long duration = System.currentTimeMillis() - time;
		logger.debug(task + " " + duration + "ms");
		time();
	}

	/**
	 * Converts a given object into pretty JSON string.
	 * 
	 * @param obj Object to convert.
	 * @return JSON string.
	 */
	public static String toPrettyJson(Object obj) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(obj);
	}

	/**
	 * Converts knime data type to a corresponding Arx data type.
	 * 
	 * @param type Knime data type.
	 * @return Arx data type.
	 */
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

	/**
	 * Converts data cell to String using formatting when necessary.
	 * 
	 * @param cell Input cell.
	 * @return String value of the cell.
	 */
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

	/**
	 * Finds a {@link Region} by it's name.
	 * 
	 * @param name Region's name.
	 * @return Region instance. <code>NONE</code> is returned in case region with a
	 *         given name is not found.
	 */
	public static Region regionByName(String name) {
		for (Region region : Region.values()) {
			if (region.getName().equals(name)) {
				return region;
			}
		}
		return Region.NONE;
	}

	/**
	 * Clones {@link HierarchyBuilder} instance.
	 * 
	 * @param h Hierarchy builder.
	 * @return Copy of a hierarchy builder.
	 * @throws IOException
	 */
	public static HierarchyBuilder<?> clone(HierarchyBuilder<?> h) throws IOException {
		File f = File.createTempFile("hierarchy", ".ahs");
		f.deleteOnExit();
		h.save(f);
		return HierarchyBuilder.create(f);
	}

	/**
	 * Adds row to a given container.
	 * 
	 * @param container Data container.
	 * @param cells     List of cells representing the row.
	 * @param index     Row index.
	 */
	public static void addRow(BufferedDataContainer container, List<DataCell> cells, long index) {
		DefaultRow row = new DefaultRow(RowKey.createRowKey(index), cells);
		container.addRowToTable(row);
	}

	/**
	 * Removes any column names from the filter that is not present in a given spec.
	 * 
	 * @param filter Settings model holding columns list.
	 * @param spec   Data table spec.
	 */
	public static void removeMissingColumns(SettingsModelFilterString filter, DataTableSpec spec) {
		List<String> filtered = filter.getIncludeList().stream().filter(name -> {
			boolean present = spec.containsName(name);
			if (!present) {
				logger.warn("Column '" + name + "' is missing from table");
			}
			return present;
		}).collect(Collectors.toList());
		filter.setIncludeList(filtered);
	}

}
