package se.redfield.arxnode.partiton;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.time.localdatetime.LocalDateTimeCell;

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
