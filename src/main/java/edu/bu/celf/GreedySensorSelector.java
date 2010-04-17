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
	private static final <K extends Comparable<K>> Comparator<Pair<Sensor<K>, Long>> benefit() {
		return new Comparator<Pair<Sensor<K>, Long>>() {
			@Override
			public int compare(Pair<Sensor<K>, Long> o1, Pair<Sensor<K>, Long> o2) {
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
	public Set<Sensor<K>> select(int budget, Set<Sensor<K>> sensors,
			CascadeSet<K> cascades) {
		final NavigableSet<Pair<Sensor<K>, Long>> benefits = new TreeSet<Pair<Sensor<K>, Long>>(
				GreedySensorSelector.<K>benefit());
		final Set<Sensor<K>> selected = new HashSet<Sensor<K>>(budget);
		// initialize benefits cache
		for(Sensor<K> sensor : sensors) {
			benefits.add(new Pair<Sensor<K>, Long>(sensor, MIN_BENEFIT));
		}
		OUTER: while(budget > 0) {
			while(!benefits.isEmpty()) {
				Pair<Sensor<K>, Long> benefit = benefits.first();
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
				benefits.add(new Pair<Sensor<K>, Long>(benefit.first, update));
			}
			// no acceptable sensor found, we're done
			break;
		}
		assert budget >= 0;
		return selected;
	}
}
