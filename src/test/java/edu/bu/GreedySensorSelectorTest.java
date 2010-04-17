package edu.bu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Test;

import edu.bu.celf.FixedCostAppraiser;
import edu.bu.celf.GreedySensorSelector;
import edu.bu.celf.IncidentDistribution;
import edu.bu.celf.Penalty;
import edu.bu.celf.SensorAppraiser;

public class GreedySensorSelectorTest {
	private final Set<Sensor<String>> allSensors = new HashSet<Sensor<String>>(2);
	private SimpleUser userA = new SimpleUser("a");
	private SimpleUser userB = new SimpleUser("b");
	private final DateTime dateTime = new DateTime();
	@SuppressWarnings("unchecked")
	private final CascadeSet<String> singleCascade = new CascadeSet<String>(Arrays.asList(new Incident<String>(
			dateTime, userA, "id"), new Incident<String>(dateTime.plus(100L), userB,
			"id")));
	private final SensorAppraiser<String> unitCost = new FixedCostAppraiser<String>(1);
	private final SensorAppraiser<String> doubleCost = new FixedCostAppraiser<String>(2);
	private final IncidentDistribution certitude = new IncidentDistribution() {
		@Override
		public <K extends Comparable<K>> double probability(IncidentCascade<K> cascade) {
			return 1.0D;
		}
		@Override
		public <K extends Comparable<K>> double probability(Incident<K> incident) {
			return 0.5D;
		}
	};

	public GreedySensorSelectorTest() {
		allSensors.add(userA);
		allSensors.add(userB);
	}

	@Test
	public void selectsHighestValueSensor() {
		Set<Sensor<String>> selection = new GreedySensorSelector<String>(unitCost,
				Penalty.<String>detectionTime(), certitude).select(1, allSensors,
				singleCascade);

		assertEquals(1, selection.size());
		assertTrue(selection.contains(userA));
	}

	@Test
	public void selectsNoSensorsWithInsufficientBudget() {
		assertEquals(0, new GreedySensorSelector<String>(unitCost,
				Penalty.<String>detectionTime(), certitude).select(0, allSensors,
				singleCascade).size());
	}

	@Test
	public void selectsAllSensorsWithSufficientBudget() {
		assertEquals(allSensors, new GreedySensorSelector<String>(unitCost,
				Penalty.<String>detectionTime(), certitude).select(allSensors.size(),
				allSensors, singleCascade));
	}
	
	@Test
	public void selectsWithinBudget() {
		assertEquals(Collections.singleton(userA), new GreedySensorSelector<String>(
				doubleCost, Penalty.<String>detectionTime(), certitude).select(3,
				allSensors, singleCascade));
	}
	
	@Test
	public void selectsWithinBudgetWithNonHomogoneousSensorCost() {
		assertEquals(Collections.singleton(userB), new GreedySensorSelector<String>(
				new SensorAppraiser<String>() {
					@Override
					public int appraise(Sensor<String> sensor) {
						if (sensor.equals(userA)) {
							return 2;
						}
						return 1;
					}
				}, Penalty.<String>detectionTime(), certitude).select(1, allSensors,
				singleCascade));
	}

}
