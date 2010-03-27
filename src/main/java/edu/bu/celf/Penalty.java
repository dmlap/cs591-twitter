/**
 * 
 */
package edu.bu.celf;

import edu.bu.CascadeSet;
import edu.bu.Incident;
import edu.bu.IncidentCascade;
import edu.bu.Sensor;

/**
 * Methods related to calculating the penalty for a given sensor placement.
 * 
 * @author dml
 * 
 */
public class Penalty {
	public static final long MAX_PENALTY = Long.MAX_VALUE;

	private final SensorEvaluator evaluator;

	public Penalty(SensorEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	/**
	 * Returns the reduction in penalty over all possible {@link Incident}s
	 * given a {@link SensorPlacement}.
	 * 
	 * @param distribution
	 *            - the {@link IncidentDistribution} for all {@link Incident
	 *            Incidents}. If the sum of the
	 *            {@link IncidentDistribution#probability(IncidentCascade)
	 *            probabilities} across all {@link IncidentCascade}s in this
	 *            {@link CascadeSet} is not in [0, 1] it is likely this method
	 *            will produce overflow or underflow.
	 * 
	 * @param cascades
	 *            - all possible {@link Incident}s
	 * @param sensors
	 *            - the {@link SensorPlacement} to evaluate
	 * @return the reduction in penalty over all possible {@link Incident}s
	 *         given a {@link SensorPlacement}.
	 */
	public long penaltyReduction(IncidentDistribution distribution,
			CascadeSet cascades, SensorPlacement sensors) {
		long result = 0L;
		for (IncidentCascade cascade : cascades) {
			result += distribution.probability(cascade)
					* penaltyReduction(cascade, sensors);
		}
		return result;
	}

	/**
	 * Returns the reduction in penalty for a given {@link IncidentCascade} and
	 * {@link SensorPlacement}.
	 * 
	 * @param incident
	 *            - the {@link IncidentCascade} to evaluate
	 * @param sensors
	 *            - the {@link SensorPlacement} to evaluate
	 * @return the reduction in penalty for a given {@link Incident} and
	 *         {@link SensorPlacement}.
	 */
	public long penaltyReduction(IncidentCascade incident,
			SensorPlacement sensors) {
		long result = MAX_PENALTY;
		for (Sensor sensor : sensors) {
			result = Math.min(result, evaluator.evaluate(sensor, incident
					.detectionDelay(sensor)));
		}
		return result;
	}

}
