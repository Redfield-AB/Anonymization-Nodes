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

import java.util.HashSet;
import java.util.Set;

/**
 * Data class to hold partition info.
 *
 */
public class PartitionInfo {

	private long rows;
	private String criteria;
	private Set<String> omittedRows;

	public PartitionInfo() {
		this.rows = 0;
		this.criteria = "";
		this.omittedRows = new HashSet<>();
	}

	/**
	 * @return Number of rows.
	 */
	public long getRows() {
		return rows;
	}

	/**
	 * @param rows Number of rows.
	 */
	public void setRows(long rows) {
		this.rows = rows;
	}

	/**
	 * @return Human readable partitioning criteria.
	 */
	public String getCriteria() {
		return criteria;
	}

	/**
	 * @param criteria Human readable partitioning criteria.
	 */
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	/**
	 * @return Collection or omitted rows in a form of RowId.
	 */
	public Set<String> getOmittedRows() {
		return omittedRows;
	}
}
