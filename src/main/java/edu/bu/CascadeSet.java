/**
 * 
 */
package edu.bu;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * A set of {@link IncidentCascade}s and methods exposing properties of the set.
 * 
 * @author dml
 * 
 */
public class CascadeSet implements Iterable<IncidentCascade> {

	private final Map<String, IncidentCascade> incidents;

	/**
	 * Construct a new {@link CascadeSet} from the specified {@link Incident}s.
	 * 
	 * @param incidents
	 */
	public CascadeSet(Incident... incidents) {
		this.incidents = new HashMap<String, IncidentCascade>(incidents.length);
		Map<String, Set<Incident>> cascades = new HashMap<String, Set<Incident>>();
		for(Incident incident : incidents) {
			if(!cascades.containsKey(incident.getIdentifier())) {
				cascades.put(incident.getIdentifier(), new HashSet<Incident>());
			}
			Set<Incident> cascade = cascades.get(incident.getIdentifier());
			cascade.add(incident);
		}
		for(Map.Entry<String, Set<Incident>> cascade : cascades.entrySet()) {
			this.incidents.put(cascade.getKey(), new IncidentCascade(cascade.getKey(), cascade.getValue()));
		}
	}

	@Override
	public Iterator<IncidentCascade> iterator() {
		return incidents.values().iterator();
	}

}
