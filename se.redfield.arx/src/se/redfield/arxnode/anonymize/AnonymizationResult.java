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
package se.redfield.arxnode.anonymize;

import java.util.Arrays;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;

import se.redfield.arxnode.partiton.PartitionInfo;
import se.redfield.arxnode.partiton.Partitioner;

/**
 * Class for holding raw results of anonymization process
 */
public class AnonymizationResult {

	private ARXResult arxResult;
	private PartitionInfo partitionInfo;
	private int[] transformation;

	/**
	 * Creates a new instance of {@link AnonymizationResult}
	 * 
	 * @param arxResult     {@link ARXResult} returned by {@link ARXAnonymizer}
	 * @param partitionInfo {@link PartitionInfo} returned by {@link Partitioner}
	 */
	public AnonymizationResult(ARXResult arxResult, PartitionInfo partitionInfo) {
		this.arxResult = arxResult;
		this.partitionInfo = partitionInfo;
		if (arxResult.isResultAvailable()) {
			this.transformation = arxResult.getGlobalOptimum().getTransformation();
		}
	}

	/**
	 * Returns the {@link ARXResult}
	 * 
	 * @return the arxResult
	 */
	public ARXResult getArxResult() {
		return arxResult;
	}

	/**
	 * Returns the partition info
	 * 
	 * @return the partition info
	 */
	public PartitionInfo getPartitionInfo() {
		return partitionInfo;
	}

	/**
	 * Returns active transformation levels
	 * 
	 * @return active transformation levels. If none of the transformations is
	 *         selected then optimum transformation is returned
	 */
	public int[] getTransformation() {
		if (transformation == null) {
			return arxResult.getGlobalOptimum().getTransformation();
		}
		return transformation;
	}

	/**
	 * 
	 * Sets active transformation levels to use
	 * 
	 * @param transformation transformation levels
	 */
	public void setTransformation(int[] transformation) {
		this.transformation = transformation;
	}

	/**
	 * @return {@link ARXNode} instance corresponding to active transformation
	 *         levels. Returns global optimum in case transformation is not selected
	 *         or corresponding node is not found.
	 */
	public ARXNode getCurrentNode() {
		return findNodeForTransfromation(transformation, true);
	}

	/**
	 * Finds the {@link ARXNode} instance corresponding to transformation levels
	 * provided
	 * 
	 * @param transform         Transformation levels of desired node
	 * @param optimumIfNotFound Option specifying if global optimum or null should
	 *                          be returned in case corresponding node is not found
	 * @return {@link ARXNode} instance with the specified transformation levels. In
	 *         case node is not found:
	 *         <ul>
	 *         <li>{@code null} is returned if {@code optimumIfNotFound} is set to
	 *         {@code false}</li>
	 *         <li>global optimum node is returned if {@code optimumIfNotFound} is
	 *         set to {@code true}</li>
	 *         </ul>
	 */
	public ARXNode findNodeForTransfromation(int[] transform, boolean optimumIfNotFound) {
		if (transform != null) {
			for (ARXNode[] nodes : arxResult.getLattice().getLevels()) {
				for (ARXNode n : nodes) {
					if (Arrays.equals(transform, n.getTransformation())) {
						return n;
					}
				}
			}
		}
		return optimumIfNotFound ? arxResult.getGlobalOptimum() : null;
	}
}
