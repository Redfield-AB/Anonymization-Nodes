package se.redfield.arxnode.partiton;

import java.util.HashSet;
import java.util.Set;

public class PartitionInfo {

	private long rows;
	private String criteria;
	private Set<String> omittedRows;

	public PartitionInfo() {
		this.rows = 0;
		this.criteria = "";
		this.omittedRows = new HashSet<>();
	}

	public long getRows() {
		return rows;
	}

	public void setRows(long rows) {
		this.rows = rows;
	}

	public String getCriteria() {
		return criteria;
	}

	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	public Set<String> getOmittedRows() {
		return omittedRows;
	}
}
