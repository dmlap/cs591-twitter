/**
 * 
 */
package edu.bu;

import org.joda.time.DateTime;

/**
 * A single occurrence of the condition under consideration.
 * 
 * @author dml
 * 
 */
public class Incident<K extends Comparable<K>> {
	private final DateTime dateTime;
	private final Sensor<K> sensor;
	private final String identifier;

	/**
	 * Constructs a new {@link Incident}
	 * 
	 * @param dateTime
	 *            - the {@link DateTime} the {@link Incident} occurred at
	 * @param sensor TODO
	 * @param identifier
	 *            - an identifier by which this {@link Incident} can be
	 *            distinguished
	 */
	public Incident(DateTime dateTime, Sensor<K> sensor, String identifier) {
		this.dateTime = dateTime;
		this.sensor = sensor;
		this.identifier = identifier;
	}

	public DateTime getDateTime() {
		return dateTime;
	}

	public Sensor<K> getSensor() {
		return sensor;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String toString() {
		return "{dateTime=" + dateTime + ", identifier=" + identifier
				+ ", sensor=" + sensor + "}";
	}
}
