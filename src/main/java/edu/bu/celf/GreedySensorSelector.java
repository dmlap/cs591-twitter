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
public class GreedySensorSelector implements SensorSelector {
	private static final Long MIN_BENEFIT = Long.MIN_VALUE;
	private static final Comparator<Pair<Sensor, Long>> BENEFIT = new Comparator<Pair<Sensor, Long>>() {
		@Override
		public int compare(Pair<Sensor, Long> o1, Pair<Sensor, Long> o2) {
			if (o1.first.equals(o2.first)) {
				return 0;
			}
			if(o1.second.equals(o2.second)) {
				return o1.first.getId().compareTo(o2.first.getId());
			}
			return o1.second.compareTo(o2.second);
		}
	};

	private final SensorAppraiser appraiser;
	private final Penalty penalty;
	private final IncidentDistribution distribution;

	public GreedySensorSelector(SensorAppraiser appraiser, Penalty penalty,
			IncidentDistribution distribution) {
		this.appraiser = appraiser;
		this.penalty = penalty;
		this.distribution = distribution;
	}

	/* (non-Javadoc)
	 * @see edu.bu.celf.SensorSelector#select(int, java.util.Set, edu.bu.CascadeSet)
	 */
	@Override
	public Set<Sensor> select(int budget, Set<Sensor> sensors,
			CascadeSet cascades) {
		final NavigableSet<Pair<Sensor, Long>> benefits = new TreeSet<Pair<Sensor, Long>>(
				BENEFIT);
		final Set<Sensor> selected = new HashSet<Sensor>(budget);
		// initialize benefits cache
		for(Sensor sensor : sensors) {
			benefits.add(new Pair<Sensor, Long>(sensor, MIN_BENEFIT));
		}
		OUTER: while(budget > 0) {
			while(!benefits.isEmpty()) {
				Pair<Sensor, Long> benefit = benefits.first();
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
				benefits.add(new Pair<Sensor, Long>(benefit.first, update));
			}
			// no acceptable sensor found, we're done
			break;
		}
		assert budget >= 0;
		return selected;
	}
}
