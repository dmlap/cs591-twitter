/**
 * 
 */
package edu.bu.celf;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.Instant;
import org.joda.time.Interval;

import edu.bu.CascadeSet;
import edu.bu.Incident;
import edu.bu.IncidentCascade;
import edu.bu.Pair;
import edu.bu.Sensor;

/**
 * Methods related to calculating the penalty for a given sensor placement.
 * 
 * @author dml
 * 
 */
public class Penalty<K extends Comparable<K>> {
	public static final <K extends Comparable<K>> Penalty<K> detectionTime(final long maxValue) {
		return new Penalty<K>(new SensorEvaluator<K>() {
				@Override
				public long evaluate(Sensor<K> sensor, Interval detectionInterval) {
					return maxValue
							- detectionInterval.toDurationMillis();
				}
			});
	}
	public static final <K extends Comparable<K>> Penalty<K> detectionLikelihood(final Instant horizon) {
		return new Penalty<K>(new SensorEvaluator<K>() {
			@Override
			public long evaluate(Sensor<K> sensor, Interval detectionInterval) {
				return horizon.isAfter(detectionInterval.getEndMillis()) ? 1L : 0L;
			}
		});
	}
	public static final <K extends Comparable<K>> Penalty<K> populationAffected(final int population, final IncidentCascade<K> cascade) {
		return new Penalty<K>(new SensorEvaluator<K>() {
			@Override
			public long evaluate(Sensor<K> sensor, Interval detectionInterval) {
				return population - cascade.predecessorCount(sensor);
			}
		});
	}

	private final SensorEvaluator<K> evaluator;

	public Penalty(SensorEvaluator<K> evaluator) {
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
			CascadeSet<K> cascades, Set<? extends Sensor<K>> sensors) {
		long result = 0L;
		for (IncidentCascade<K> cascade : cascades) {
			result += // distribution.probability(cascade) *
					penaltyReduction(cascade, sensors);
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
	protected long penaltyReduction(IncidentCascade<K> incident,
			Set<? extends Sensor<K>> sensors) {
		long result = 0L;
		for (Sensor<K> sensor : sensors) {
			result = Math.max(result, evaluator.evaluate(sensor, incident
					.detectionDelay(sensor)));
		}
		return result;
	}

	/**
	 * Constructs a new {@link Penalty} by adding together the supplied
	 * penalties with an associated scaling factor.
	 * @param penalties
	 *            - the {@link Pair}s of scaling factors and {@link Penalty
	 *            penalties} to accumulate.
	 * 
	 * @return a new {@link Penalty}
	 */
	public static <K extends Comparable<K>> Penalty<K> compose(
			final List<Pair<Double, Penalty<K>>> penalties) {
		return new Penalty<K>(new SensorEvaluator<K>() {
			@Override
			public long evaluate(Sensor<K> sensor, Interval detectionInterval) {
				long result = 0;
				for (Pair<Double, Penalty<K>> pair : penalties) {
					result += pair.first
							* pair.second.evaluator.evaluate(sensor,
									detectionInterval);
				}
				return result;
			}
		});
	}

}
