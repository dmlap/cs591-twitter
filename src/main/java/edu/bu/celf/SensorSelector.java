/**
 * 
 */
package edu.bu.celf;

import java.util.Set;

import edu.bu.CascadeSet;
import edu.bu.Sensor;

/**
 * An interface for objects which select a {@link Set subset} of {@link Sensor}s
 * given a {@link CascadeSet}.
 * 
 * @author dml
 * 
 */
public interface SensorSelector {

	/**
	 * Returns a {@link Set subset} of sensors with cost no greater than
	 * <code>budget</code>
	 * 
	 * @param sensors
	 *            - the {@link Set} of all possible {@link Sensor}s to select
	 *            from.
	 * @param cascades
	 *            - the {@link CascadeSet} to inform {@link Sensor} selection
	 * @return a {@link Set subset} of sensors with cost no greater than
	 *         <code>budget</code>
	 */
	Set<Sensor> select(int budget, Set<Sensor> sensors, CascadeSet cascades);

}
