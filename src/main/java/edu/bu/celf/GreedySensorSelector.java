/**
 * 
 */
package edu.bu.celf;

import java.util.Collections;
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
	private final NavigableSet<Pair<Sensor, Long>> benefits = new TreeSet<Pair<Sensor, Long>>(
			BENEFIT);
	private final Penalty penalty;
	private final IncidentDistribution distribution;

	public GreedySensorSelector(SensorAppraiser appraiser, Penalty penalty,
			IncidentDistribution distribution) {
		this.appraiser = appraiser;
		this.penalty = penalty;
		this.distribution = distribution;
	}

	@Override
	public Set<Sensor> select(int budget, Set<Sensor> sensors,
			CascadeSet cascades) {
		// initialize benefits cache
		for(Sensor sensor : sensors) {
			benefits.add(new Pair<Sensor, Long>(sensor, MIN_BENEFIT));
		}
		int cost = 0;
		Set<Sensor> unselectedSensors = new HashSet<Sensor>(sensors);
		Set<Sensor> selectedSensors = new HashSet<Sensor>();
		Set<Sensor> selected;
		do {
			selected = new GreedySelectorRound().select(budget - cost, unselectedSensors, cascades);
			assert selected.size() <= 1 : "GreedySelectorRound should select no more than one sensor per iteration";
			for(Sensor sensor: selected) {
				cost += appraiser.appraise(sensor);
			}
			unselectedSensors.removeAll(selected);
			selectedSensors.addAll(selected);
		} while(selected.size() > 0);
		return selectedSensors;
	}
	
	/**
	 * An object which performs a single round of the
	 * {@link GreedySensorSelector} algorithm.
	 * 
	 * @author dml
	 * 
	 */
	private class GreedySelectorRound implements SensorSelector {
		@Override
		public Set<Sensor> select(int budget, Set<Sensor> sensors,
				CascadeSet cascades) {
			if(budget <= 0) {
				return Collections.emptySet();
			}
			Pair<Sensor, Long> focus, head;
			for(int i = 0; i < sensors.size(); i++) {
				head = benefits.pollFirst();
				focus = new Pair<Sensor, Long>(head.first, penalty
						.penaltyReduction(distribution, cascades, sensors));
				benefits.add(focus);
				if(BENEFIT.compare(head, focus) >= 0) {
					// focus is the best sensor we can find
					return appraiser.appraise(focus.first) > budget ? Collections.<Sensor>emptySet() : Collections.singleton(focus.first);
				}
			}
			// every available sensor is unsuitable
			return Collections.emptySet();
		}
	}
}
