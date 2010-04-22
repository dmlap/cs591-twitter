/**
 * 
 */
package edu.bu.celf;

import java.util.Comparator;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import edu.bu.CascadeSet;
import edu.bu.Pair;
import edu.bu.Sensor;

/**
 * A {@link SensorSelector} that utilizes a greedy selection algorithm.
 * 
 * @author dml
 * 
 */
public class GreedySensorSelector<K extends Comparable<K>> implements SensorSelector<K> {
	private static final Long MIN_BENEFIT = Long.MIN_VALUE;
	private static final <K extends Comparable<K>, S extends Sensor<K>> Comparator<Pair<S, Long>> benefit() {
		return new Comparator<Pair<S, Long>>() {
			@Override
			public int compare(Pair<S, Long> o1, Pair<S, Long> o2) {
				if (o1.first.equals(o2.first)) {
					return 0;
				}
				if(o1.second.equals(o2.second)) {
					return o1.first.getId().compareTo(o2.first.getId());
				}
				return o1.second.compareTo(o2.second);
			}
		};
	}

	private final SensorAppraiser<K> appraiser;
	private final Penalty<K> penalty;
	private final IncidentDistribution distribution;

	public GreedySensorSelector(SensorAppraiser<K> appraiser, Penalty<K> penalty,
			IncidentDistribution distribution) {
		this.appraiser = appraiser;
		this.penalty = penalty;
		this.distribution = distribution;
	}

	/* (non-Javadoc)
	 * @see edu.bu.celf.SensorSelector#select(int, java.util.Set, edu.bu.CascadeSet)
	 */
	@Override
	public <S extends Sensor<K>> Set<S> select(int budget, Set<S> sensors,
			CascadeSet<K> cascades) {
		final NavigableSet<Pair<S, Long>> benefits = new TreeSet<Pair<S, Long>>(
				GreedySensorSelector.<K, S>benefit());
		final Set<S> selected = new HashSet<S>(budget);
		// initialize benefits cache
		for(S sensor : sensors) {
			benefits.add(new Pair<S, Long>(sensor, MIN_BENEFIT));
		}
		OUTER: while(budget > 0) {
			while(!benefits.isEmpty()) {
				Pair<S, Long> benefit = benefits.first();
				benefits.remove(benefit);
				int cost = appraiser.appraise(benefit.first); 
				if(cost > budget) {
					// this sensor is too expensive, skip it
					continue;
				}
				selected.add(benefit.first);
				long update = penalty.penaltyReduction(distribution, cascades, selected);
				if(benefits.isEmpty() || update > benefits.first().second) {
					// found the best possible sensor for this iteration
					budget -= cost; 
					continue OUTER;
				}
				selected.remove(benefit.first);
				benefits.add(new Pair<S, Long>(benefit.first, update));
			}
			// no acceptable sensor found, we're done
			break;
		}
		assert budget >= 0;
		return selected;
	}
}
