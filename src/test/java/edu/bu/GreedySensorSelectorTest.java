package edu.bu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import edu.bu.celf.FixedCostAppraiser;
import edu.bu.celf.GreedySensorSelector;
import edu.bu.celf.IncidentDistribution;
import edu.bu.celf.Penalty;
import edu.bu.celf.SensorAppraiser;

public class GreedySensorSelectorTest {
	private final Set<Sensor> allSensors = new HashSet<Sensor>(2);
	private TwitterUser userA = new TwitterUser("a");
	private TwitterUser userB = new TwitterUser("b");
	private final DateTime dateTime = new DateTime();
	private final CascadeSet singleCascade = new CascadeSet(new Incident(
			dateTime, userA, "id"), new Incident(dateTime.plus(100L), userB,
			"id"));
	private final SensorAppraiser unitCost = new FixedCostAppraiser(1);
	private final SensorAppraiser doubleCost = new FixedCostAppraiser(2);
	private final IncidentDistribution certitude = new IncidentDistribution() {
		@Override
		public double probability(IncidentCascade cascade) {
			return 1.0D;
		}
		@Override
		public double probability(Incident incident) {
			return 0.5D;
		}
	};

	public GreedySensorSelectorTest() {
		allSensors.add(userA);
		allSensors.add(userB);
	}

	@Test
	public void selectsHighestValueSensor() {
		Set<Sensor> selection = new GreedySensorSelector(unitCost,
				Penalty.INTERVAL_PENALTY, certitude).select(1, allSensors,
				singleCascade);

		assertEquals(1, selection.size());
		assertTrue(selection.contains(userA));
	}

	@Test
	public void selectsNoSensorsWithInsufficientBudget() {
		assertEquals(0, new GreedySensorSelector(unitCost,
				Penalty.INTERVAL_PENALTY, certitude).select(0, allSensors,
				singleCascade).size());
	}

	@Test
	public void selectsAllSensorsWithSufficientBudget() {
		assertEquals(allSensors, new GreedySensorSelector(unitCost,
				Penalty.INTERVAL_PENALTY, certitude).select(allSensors.size(),
				allSensors, singleCascade));
	}
	
	@Test
	public void selectsWithinBudget() {
		assertEquals(Collections.singleton(userA), new GreedySensorSelector(
				doubleCost, Penalty.INTERVAL_PENALTY, certitude).select(3,
				allSensors, singleCascade));
	}
	
	@Test
	@Ignore("not implemented")
	public void selectsWithinBudgetWithNonHomogoneousSensorCost() {
		assertEquals(Collections.singleton(userB), new GreedySensorSelector(
				new SensorAppraiser() {
					@Override
					public int appraise(Sensor sensor) {
						if (sensor.equals(userA)) {
							return 2;
						}
						return 1;
					}
				}, Penalty.INTERVAL_PENALTY, certitude).select(1, allSensors,
				singleCascade));
	}

}
