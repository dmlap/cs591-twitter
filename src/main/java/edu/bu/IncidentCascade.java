/**
 * 
 */
package edu.bu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

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
	private static final Sensor ENDTIME_SENSOR = new Sensor() {
		@Override
		public String getId() {
			return "**Dummy sensor**";
		}
	};
	private static final Interval MAX_INTERVAL = new Interval(0L, Long.MAX_VALUE);
	
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
	private final Incident source;

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
		this.incidents = new HashMap<Sensor, NavigableSet<DateTime>>(incidents
				.size());
		Incident source = new Incident(new DateTime(Long.MAX_VALUE), ENDTIME_SENSOR , identifier);
		for (Sensor sensor : incidents.keySet()) {
			NavigableSet<DateTime> occurrences = new TreeSet<DateTime>(
					incidents.get(sensor));
			this.incidents.put(sensor, occurrences);
			if (source.getDateTime().isAfter(occurrences.first())) {
				source = new Incident(occurrences.first(), sensor,
						this.identifier);
			}
		}
		this.source = source;
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
		Incident source = new Incident(new DateTime(Long.MAX_VALUE), ENDTIME_SENSOR , identifier);
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
			if(incident.getDateTime().isBefore(source.getDateTime())) {
				source = incident;
			}
		}
		this.source = source;
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
		this.identifier = incident.getIdentifier();
		this.incidents = new HashMap<Sensor, NavigableSet<DateTime>>(incidents.length + 1);
		Incident source = incident;
		this.incidents.put(source.getSensor(), new TreeSet<DateTime>(Collections.singleton(source.getDateTime())));
		for (Incident i : incidents) {
			if (!this.identifier.equals(i.getIdentifier())) {
				throw new IllegalArgumentException(
						"All members of an IncidentCascade must have the same identifier.  Expecting \""
								+ this.identifier
								+ "\" but found \""
								+ i.getIdentifier() + "\"");
			}
			if(!this.incidents.containsKey(i.getSensor())) {
				this.incidents.put(i.getSensor(), new TreeSet<DateTime>());
			}
			if(source.getDateTime().isAfter(i.getDateTime())) {
				source = i;
			}
			this.incidents.get(i.getSensor()).add(i.getDateTime());
		}
		this.source = source;
	}

	/**
	 * Returns the earliest occurring {@link Incident} for this
	 * {@link IncidentCascade}.
	 * 
	 * @return the earliest occurring {@link Incident} for this
	 *         {@link IncidentCascade}.
	 */
	public Incident getSource() {
		return source;
	}
	
	public Interval detectionDelay(Sensor sensor) {
		if(!incidents.containsKey(sensor)) {
			return MAX_INTERVAL;
		}
		DateTime first = incidents.get(sensor).first();
		return new Interval(source.getDateTime(), first);
	}

	public String getIdentifier() {
		return identifier;
	}

}
