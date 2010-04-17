/**
 * 
 */
package edu.bu.celf;

import java.util.Set;

import org.joda.time.Instant;
import org.joda.time.Interval;

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
	public static final Penalty DETECTION_TIME = new Penalty(
			new SensorEvaluator() {
				@Override
				public long evaluate(Sensor sensor, Interval detectionInterval) {
					return Long.MAX_VALUE
							- detectionInterval.toDurationMillis();
				}
			});
	public static final Penalty detectionLikelihood(final Instant horizon) {
		return new Penalty(new SensorEvaluator() {
			@Override
			public long evaluate(Sensor sensor, Interval detectionInterval) {
				return horizon.isAfter(detectionInterval.getEndMillis()) ? 1L : 0L;
			}
		});
	}
	public static final Penalty populationAffected(final IncidentCascade cascade) {
		return new Penalty(new SensorEvaluator() {
			@Override
			public long evaluate(Sensor sensor, Interval detectionInterval) {
				return -cascade.predecessorCount(sensor);
			}
		});
	}

	private final SensorEvaluator evaluator;

	public Penalty(SensorEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	/**
	 * Returns the reduction in penalty over all possible {@link Incident}s
	 * given a {@link Set} of {@link Sensor}s.
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
	 *            - the {@link Set} of {@link Sensor}s to evaluate
	 * @return the reduction in penalty over all possible {@link Incident}s
	 *         given a {@link Set} of {@link Sensor}s.
	 */
	public long penaltyReduction(IncidentDistribution distribution,
			CascadeSet cascades, Set<Sensor> sensors) {
		long result = 0L;
		for (IncidentCascade cascade : cascades) {
			result += distribution.probability(cascade)
					* penaltyReduction(cascade, sensors);
		}
		return result;
	}

	/**
	 * Returns the reduction in penalty for a given {@link IncidentCascade} and
	 * {@link Set} of {@link Sensor}s.
	 * 
	 * @param incident
	 *            - the {@link IncidentCascade} to evaluate
	 * @param sensors
	 *            - the {@link Set} of {@link Sensor}s to evaluate
	 * @return the reduction in penalty for a given {@link Incident} and
	 *         {@link Set} of {@link Sensor}s
	 */
	protected long penaltyReduction(IncidentCascade incident,
			Set<Sensor> sensors) {
		long result = MAX_PENALTY;
		for (Sensor sensor : sensors) {
			result = Math.min(result, evaluator.evaluate(sensor, incident
					.detectionDelay(sensor)));
		}
		return result;
	}

}
