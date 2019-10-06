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

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;

public class EqualSizePartitioner extends Partitioner {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(EqualSizePartitioner.class);

	private List<Partition> partitions;
	private long partitionSize;

	public EqualSizePartitioner(int partsNum) {
		super(partsNum);
	}

	@Override
	protected void init(BufferedDataTable source) {
		if (source.size() < partsNum) {
			partsNum = (int) source.size();
		}
		partitionSize = source.size() / partsNum;
		partitions = new ArrayList<>();
		for (int i = 0; i < partsNum; i++) {
			partitions.add(new Partition(createData(source)));
		}
	}

	@Override
	protected Partition findTarget(DataRow row, long index) {
		int idx = Math.min((int) (index / partitionSize), partitions.size() - 1);
		return partitions.get(idx);
	}

	@Override
	protected List<Partition> getResult() {
		return partitions;
	}
}
