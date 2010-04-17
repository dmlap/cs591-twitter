/**
 * 
 */
package edu.bu.celf;

import edu.bu.Sensor;

/**
 * A {@link SensorAppraiser} that returns the same cost for all {@link Sensor}s
 * 
 * @author dml
 * 
 */
public class FixedCostAppraiser<K extends Comparable<K>> implements SensorAppraiser<K> {
	private final int cost;

	public FixedCostAppraiser(int cost) {
		this.cost = cost;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.bu.celf.SensorAppraiser#appraise(edu.bu.Sensor)
	 */
	@Override
	public int appraise(Sensor<K> sensor) {
		return cost;
	}

}
