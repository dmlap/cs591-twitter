/**
 * 
 */
package edu.bu.celf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.bu.Sensor;

/**
 * A collection of {@link Sensor}s.
 * 
 * @author dml
 * 
 */
public class SensorPlacement<K extends Comparable<K>> implements Set<Sensor<K>> {
	private final Map<K, Sensor<K>> sensors;

	public SensorPlacement(Iterable<? extends Sensor<K>> sensors) {
		this.sensors = new HashMap<K, Sensor<K>>();
		for (Sensor<K> sensor : sensors) {
			this.sensors.put(sensor.getId(), sensor);
		}
	}

	/**
	 * Returns whether this {@link SensorPlacement} includes the specified
	 * {@link Sensor}
	 * 
	 * @param sensor
	 *            - the {@link Sensor} to test for
	 * @return whether this {@link SensorPlacement} includes the specified
	 *         {@link Sensor}
	 */
	public boolean contains(Sensor<K> sensor) {
		return sensors.containsKey(sensor.getId());
	}

	@Override
	public boolean add(Sensor<K> e) {
		throw new UnsupportedOperationException("SensorPlacements are immutable");
	}

	@Override
	public boolean addAll(Collection<? extends Sensor<K>> c) {
		throw new UnsupportedOperationException("SensorPlacements are immutable");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("SensorPlacements are immutable");
	}

	@Override
	public boolean contains(Object o) {
		return sensors.values().contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return sensors.values().containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return sensors.isEmpty();
	}

	@Override
	public Iterator<Sensor<K>> iterator() {
		return sensors.values().iterator();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("SensorPlacements are immutable");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("SensorPlacements are immutable");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("SensorPlacements are immutable");
	}

	@Override
	public int size() {
		return sensors.size();
	}

	@Override
	public Object[] toArray() {
		return sensors.values().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return sensors.values().toArray(a);
	}

}
