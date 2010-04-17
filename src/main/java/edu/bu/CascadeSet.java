/**
 * 
 */
package edu.bu;

import java.util.Collection;
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
public class CascadeSet<K extends Comparable<K>> implements Iterable<IncidentCascade<K>> {

	private final Map<String, IncidentCascade<K>> incidents;

	/**
	 * Construct a new {@link CascadeSet} from the specified {@link Incident}s.
	 * 
	 * @param incidents
	 */
	public CascadeSet(Collection<? extends Incident<K>> incidents) {
		this.incidents = new HashMap<String, IncidentCascade<K>>(incidents.size());
		Map<String, Set<Incident<K>>> cascades = new HashMap<String, Set<Incident<K>>>();
		for(Incident<K> incident : incidents) {
			if(!cascades.containsKey(incident.getIdentifier())) {
				cascades.put(incident.getIdentifier(), new HashSet<Incident<K>>());
			}
			Set<Incident<K>> cascade = cascades.get(incident.getIdentifier());
			cascade.add(incident);
		}
		for(Map.Entry<String, Set<Incident<K>>> cascade : cascades.entrySet()) {
			this.incidents.put(cascade.getKey(), new IncidentCascade<K>(cascade.getKey(), cascade.getValue()));
		}
	}

	@Override
	public Iterator<IncidentCascade<K>> iterator() {
		return incidents.values().iterator();
	}

}
