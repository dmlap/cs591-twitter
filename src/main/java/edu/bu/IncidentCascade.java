/**
 * 
 */
package edu.bu;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * A series of {@link Incident}s all with the same
 * {@link Incident#getIdentifier() identifier}.
 * 
 * @author dml
 * 
 */
public class IncidentCascade<K extends Comparable<K>> {
	private static final Interval MAX_INTERVAL = new Interval(0L, Long.MAX_VALUE);

	private static final <K extends Comparable<K>> Comparator<Incident<K>> firstDetection() {
		return new Comparator<Incident<K>>() {
			@Override
			public int compare(Incident<K> lhs, Incident<K> rhs) {
				return lhs.getDateTime().compareTo(rhs.getDateTime());
			}
		};
	}
	
	/**
	 * Returns an {@link IncidentCascade} consisting of a single
	 * {@link Incident}.
	 * 
	 * @param incident
	 *            - the only member of this {@link IncidentCascade}
	 * @return an {@link IncidentCascade} consisting of a single
	 *         {@link Incident}.
	 */
	public static <K extends Comparable<K>> IncidentCascade<K> singleton(Incident<K> incident) {
		return new IncidentCascade<K>(incident.getIdentifier(), Collections
				.singletonMap(incident.getSensor(), Collections
						.singleton(incident.getDateTime())));
	}

	private final String identifier;
	private final Map<Sensor<K>, NavigableSet<DateTime>> incidents;
	private final NavigableSet<Incident<K>> detections;

	/**
	 * Construct a new {@link IncidentCascade} for the given
	 * {@link Incident#getIdentifier() identifier} with the specified
	 * {@link Incident}s
	 * 
	 * @param identifier
	 *            - the id of the {@link Incident}
	 * @param incidents
	 *            - a {@link Map} of {@link Sensor}s to {@link DateTime
	 *            detection times}.
	 */
	public IncidentCascade(String identifier,
			Map<Sensor<K>, Set<DateTime>> incidents) {
		if(incidents.size() < 1) {
			throw new IllegalArgumentException("IncidentCascades must be constructed with at least one Incident.");
		}
		this.identifier = identifier;
		this.incidents = new HashMap<Sensor<K>, NavigableSet<DateTime>>(incidents.size());
		this.detections = new TreeSet<Incident<K>>(IncidentCascade.<K>firstDetection());
		for (Sensor<K> sensor : incidents.keySet()) {
			NavigableSet<DateTime> occurrences = new TreeSet<DateTime>(
					incidents.get(sensor));
			this.incidents.put(sensor, occurrences);
			this.detections.add(new Incident<K>(occurrences.first(), sensor, identifier));
		}
	}
	
	/**
	 * Constructs a new {@link IncidentCascade} from a {@link Set} of
	 * {@link Incident}s with a specified identifier.
	 * 
	 * @param identifier
	 *            - the identifier of all {@link Incident}s in the {@link Set}
	 * @param incidents
	 *            - the {@link Incident}s to form this {@link IncidentCascade}
	 */
	public IncidentCascade(String identifier, Set<Incident<K>> incidents) {
		if(incidents.size() < 1) {
			throw new IllegalArgumentException("IncidentCascades must be constructed with at least one Incident.");
		}
		this.identifier = identifier;
		this.incidents = new HashMap<Sensor<K>, NavigableSet<DateTime>>(incidents.size());
		this.detections = new TreeSet<Incident<K>>(IncidentCascade.<K>firstDetection());
		for(Incident<K> incident : incidents) {
			if (!identifier.equals(incident.getIdentifier())) {
				throw new IllegalArgumentException(
						"IncidentCascade constructed with identifier \""
								+ identifier
								+ "\" but encountered Incident with id \""
								+ incident.getIdentifier() + "\"");
			}
			if(!this.incidents.containsKey(incident.getSensor())) {
				this.incidents.put(incident.getSensor(), new TreeSet<DateTime>());
			}
			NavigableSet<DateTime> times = this.incidents.get(incident.getSensor());
			times.add(incident.getDateTime());
		}
		for(Entry<Sensor<K>, NavigableSet<DateTime>> entry : this.incidents.entrySet()) {
			this.detections.add(new Incident<K>(entry.getValue().first(), entry.getKey(), identifier));
		}
	}

	/**
	 * Returns the earliest occurring {@link Incident} for this
	 * {@link IncidentCascade}.
	 * 
	 * @return the earliest occurring {@link Incident} for this
	 *         {@link IncidentCascade}.
	 */
	public Incident<K> getSource() {
		return detections.first();
	}
	
	public Interval detectionDelay(Sensor<K> sensor) {
		if(!incidents.containsKey(sensor)) {
			return MAX_INTERVAL;
		}
		DateTime first = incidents.get(sensor).first();
		return new Interval(getSource().getDateTime(), first);
	}

	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the number of {@link Sensor}s that have detected an
	 * {@link Incident} before the specified {@link Sensor}.
	 * 
	 * @param sensor
	 *            - the {@link Sensor} to query
	 * @return the number of {@link Sensor}s that have detected an
	 *         {@link Incident} before the specified {@link Sensor}.
	 */
	public int predecessorCount(Sensor<K> sensor) {
		if(!incidents.containsKey(sensor)) {
			// never detected at the specified sensor
			return this.detections.size();
		}
		DateTime firstDetection = incidents.get(sensor).first();
		return detections.headSet(new Incident<K>(firstDetection, sensor, identifier)).size();
	}

}
