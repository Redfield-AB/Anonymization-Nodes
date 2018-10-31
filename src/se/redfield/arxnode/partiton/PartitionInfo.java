package se.redfield.arxnode.partiton;

public class PartitionInfo {

	private long rows;
	private String criteria;

	public PartitionInfo(long rows, String criteria) {
		this.rows = rows;
		this.criteria = criteria;
	}

	public long getRows() {
		return rows;
	}

	public String getCriteria() {
		return criteria;
	}
}
