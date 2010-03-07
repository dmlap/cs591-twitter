/**
 * 
 */
package edu.bu.celf;

import java.util.HashMap;
import java.util.Map;

import edu.bu.Sensor;

/**
 * A collection of {@link Sensor}s.
 * 
 * @author dml
 * 
 */
public class SensorPlacement {
	private final Map<String, Sensor> sensors;
	
	public SensorPlacement(Sensor...sensors) {
		this.sensors = new HashMap<String, Sensor>(sensors.length);
		for(Sensor sensor : sensors) {
			this.sensors.put(sensor.getId(), sensor);
		}
	}

}
