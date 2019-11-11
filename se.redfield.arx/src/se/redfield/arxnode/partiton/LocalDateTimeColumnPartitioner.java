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
package se.redfield.arxnode.partiton;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.time.localdatetime.LocalDateTimeCell;

/**
 * Grouping partitioner for {@link LocalDateTime} columns. Works the same way as
 * {@link DoubleColumnPartitioner}.
 *
 */
public class LocalDateTimeColumnPartitioner extends DoubleColumnPartitioner {

	public LocalDateTimeColumnPartitioner(String column, int partsNum) {
		super(column, partsNum);
	}

	@Override
	protected Double getValue(DataRow row) {
		DataCell cell = row.getCell(columnIndex);
		if (cell.isMissing()) {
			return null;
		}
		LocalDateTime time = ((LocalDateTimeCell) cell).getLocalDateTime();
		return (double) time.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
	}

	@Override
	protected String createCriteria(int index) {
		double start = min + intervalLength * index;
		LocalDateTime t1 = LocalDateTime.ofInstant(Instant.ofEpochSecond((long) start), ZoneId.systemDefault());
		LocalDateTime t2 = LocalDateTime.ofInstant(Instant.ofEpochSecond((long) (start + intervalLength)),
				ZoneId.systemDefault());
		return String.format("%s in [%s, %s]", column, t1, t2);
	}
}
