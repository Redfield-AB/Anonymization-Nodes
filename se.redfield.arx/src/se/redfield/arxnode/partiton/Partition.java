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

import org.deidentifier.arx.Data.DefaultData;

/**
 * Data class to hold a partition.
 *
 */
public class Partition {

	private DefaultData data;
	private PartitionInfo info;

	/**
	 * @param data Arx data instance.
	 */
	public Partition(DefaultData data) {
		this.data = data;
		this.info = new PartitionInfo();
	}

	public DefaultData getData() {
		return data;
	}

	public PartitionInfo getInfo() {
		return info;
	}
}
