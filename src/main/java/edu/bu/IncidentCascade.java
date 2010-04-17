/**
 * 
 */
package edu.bu;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
public class IncidentCascade {
	private static final Interval MAX_INTERVAL = new Interval(0L, Long.MAX_VALUE);
	private static final Comparator<Incident> FIRST_DETECTION = new Comparator<Incident>() {
		@Override
		public int compare(Incident lhs, Incident rhs) {
			return lhs.getDateTime().compareTo(rhs.getDateTime());
		}
	};
	
	/**
	 * Returns a new {@link List} by concatenating its two arguments
	 * 
	 * @param <T>
	 *            - the type of elements of the resulting {@link List}
	 * @param lhs
	 *            - the first elements
	 * @param rhs
	 *            - the second elements
	 * @return a new {@link List}
	 */
	private static <T> List<T> join(T[] lhs, T... rhs) {
		List<T> result = new LinkedList<T>(Arrays.asList(lhs));
		result.addAll(Arrays.asList(rhs));
		return result;
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
	public static IncidentCascade singleton(Incident incident) {
		return new IncidentCascade(incident.getIdentifier(), Collections
				.singletonMap(incident.getSensor(), Collections
						.singleton(incident.getDateTime())));
	}

	private final String identifier;
	private final Map<Sensor, NavigableSet<DateTime>> incidents;
	private final NavigableSet<Incident> detections;

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
			Map<Sensor, Set<DateTime>> incidents) {
		if(incidents.size() < 1) {
			throw new IllegalArgumentException("IncidentCascades must be constructed with at least one Incident.");
		}
		this.identifier = identifier;
		this.incidents = new HashMap<Sensor, NavigableSet<DateTime>>(incidents.size());
		this.detections = new TreeSet<Incident>(FIRST_DETECTION);
		for (Sensor sensor : incidents.keySet()) {
			NavigableSet<DateTime> occurrences = new TreeSet<DateTime>(
					incidents.get(sensor));
			this.incidents.put(sensor, occurrences);
			this.detections.add(new Incident(occurrences.first(), sensor, identifier));
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
	public IncidentCascade(String identifier, Set<Incident> incidents) {
		if(incidents.size() < 1) {
			throw new IllegalArgumentException("IncidentCascades must be constructed with at least one Incident.");
		}
		this.identifier = identifier;
		this.incidents = new HashMap<Sensor, NavigableSet<DateTime>>(incidents.size());
		this.detections = new TreeSet<Incident>(FIRST_DETECTION);
		for(Incident incident : incidents) {
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
		for(Entry<Sensor, NavigableSet<DateTime>> entry : this.incidents.entrySet()) {
			this.detections.add(new Incident(entry.getValue().first(), entry.getKey(), identifier));
		}
	}

	/**
	 * Constructs a new {@link IncidentCascade} from a set of {@link Incident}s.
	 * 
	 * @param incident
	 *            - an {@link Incident} that is a member of this
	 *            {@link IncidentCascade}
	 * @param incidents
	 *            - a variable number of {@link Incident}s, all with the same
	 *            {@link Incident#getIdentifier() identifier}.
	 */
	public IncidentCascade(Incident incident, Incident... incidents) {
		this(incident.getIdentifier(), new HashSet<Incident>(join(incidents, incident)));
	}

	/**
	 * Returns the earliest occurring {@link Incident} for this
	 * {@link IncidentCascade}.
	 * 
	 * @return the earliest occurring {@link Incident} for this
	 *         {@link IncidentCascade}.
	 */
	public Incident getSource() {
		return detections.first();
	}
	
	public Interval detectionDelay(Sensor sensor) {
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
	public int predecessorCount(Sensor sensor) {
		if(!incidents.containsKey(sensor)) {
			// never detected at the specified sensor
			return this.detections.size();
		}
		DateTime firstDetection = incidents.get(sensor).first();
		return detections.headSet(new Incident(firstDetection, sensor, identifier)).size();
	}

}
