/**
 * 
 */
package edu.bu.celf;

import org.joda.time.Interval;

import edu.bu.Incident;
import edu.bu.Sensor;

/**
 * An interface for objects which can score a particular {@link Incident} given
 * a detection {@link Interval}.
 * 
 * @author dml
 * 
 */
public interface SensorEvaluator {

	/**
	 * Returns the score for detecting an {@link Incident} at a {@link Sensor}
	 * with the given {@link Interval}.
	 * 
	 * @param sensor
	 *            - the {@link Sensor} to evaluate for
	 * @param detectionInterval
	 *            - the length of time until the {@link Incident} is detected
	 * @return the score for detecting an {@link Incident} at a {@link Sensor}
	 *         with the given {@link Interval}.
	 */
	long evaluate(Sensor sensor, Interval detectionInterval);

}
