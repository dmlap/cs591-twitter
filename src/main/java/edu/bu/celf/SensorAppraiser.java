/**
 * 
 */
package edu.bu.celf;

import edu.bu.Sensor;

/**
 * An interface for objects which can determine the cost of a {@link Sensor}.
 * 
 * @author dml
 * 
 */
public interface SensorAppraiser<K extends Comparable<K>> {

	/**
	 * Returns the cost of a {@link Sensor}
	 * 
	 * @param sensor
	 *            - the {@link Sensor} to appraise
	 * @return the cost of a {@link Sensor}
	 */
	int appraise(Sensor<K> sensor);

}
