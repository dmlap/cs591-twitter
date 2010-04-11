/**
 * 
 */
package edu.bu.celf;

import edu.bu.Incident;
import edu.bu.IncidentCascade;


/**
 * An interface for objects which specify a probability distribution over
 * {@link Incident}s.
 * 
 * @author dml
 * 
 */
public interface IncidentDistribution {

	/**
	 * Returns the probability of the occurrence of a specified {@link Incident}
	 * . The return value should be within [0, 1].
	 * 
	 * @param incident
	 *            - the {@link Incident} to query
	 * @return the probability of the occurrence of a specified {@link Incident}
	 */
	double probability(Incident incident);
	
	/**
	 * Returns the probability of the occurrence of a specified
	 * {@link IncidentCascade}. The return value should be within [0, 1].
	 * 
	 * @param cascade
	 *            - the {@link IncidentCascade} to query
	 * @return the probability of the occurrence of a specified
	 *         {@link IncidentCascade}
	 */
	double probability(IncidentCascade cascade);

}
