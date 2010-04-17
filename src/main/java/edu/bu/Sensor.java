/**
 * 
 */
package edu.bu;

/**
 * An interface for objects which represent an {@link Incident} sensor.
 * 
 * @author dml
 * 
 */
public interface Sensor<K extends Comparable<K>> {
	
	/**
	 * Returns the identifier for this {@link Sensor}
	 * 
	 * @return the identifier for this {@link Sensor}
	 */
	K getId();

}
