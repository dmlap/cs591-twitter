package edu.bu.celf;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.junit.Test;

import edu.bu.CascadeSet;
import edu.bu.Incident;
import edu.bu.IncidentCascade;
import edu.bu.TestUser;

public class PenaltyTest {

	@Test
	public void notDetectedIncidentEqualsZeroReduction() {
		Incident<String> incident = new Incident<String>(new DateTime(), new TestUser(
				"user-a"), "undetected-incident");
		SensorPlacement<String> sensors = new SensorPlacement<String>(singleton(new TestUser("user-b")));
		assertEquals(0, Penalty.<String>detectionTime().penaltyReduction(IncidentCascade
				.singleton(incident), sensors));
	}

	@Test
	public void detectedIncidentReductionProportionalToDetectionTime() {
		TestUser userA = new TestUser("userA");
		TestUser userB = new TestUser("userB");
		TestUser userC = new TestUser("userC");
		SensorPlacement<String> onlyA = new SensorPlacement<String>(singleton(userA));
		SensorPlacement<String> onlyB = new SensorPlacement<String>(singleton(userB));
		SensorPlacement<String> onlyC = new SensorPlacement<String>(singleton(userC));
		DateTime time0 = new DateTime();
		DateTime time1 = new DateTime(time0.getMillis() + 1);

		Set<Incident<String>> incidents = new HashSet<Incident<String>>();
		incidents.add(new Incident<String>(time0,
				userA, "id"));
		incidents.add(new Incident<String>(time1, userB, "id"));
		IncidentCascade<String> cascade = new IncidentCascade<String>("id", incidents);

		long atTime0 = Penalty.<String>detectionTime().penaltyReduction(cascade, onlyA);
		long atTime1 = Penalty.<String>detectionTime().penaltyReduction(cascade, onlyB);
		long atNever = Penalty.<String>detectionTime().penaltyReduction(cascade, onlyC);

		assertTrue(
				"Penalty reduction for detecting at time 0 should be greater than detection at time 1.  Time 0: "
						+ atTime0 + ", Time1: " + atTime1, atTime0 > atTime1);
		assertTrue(
				"Penalty reduction for detecting at time 0 should be greater than never detecting.  Time 0: "
						+ atTime0 + ", never: " + atNever, atTime0 > atNever);
		assertTrue(
				"Penalty reduction for detecting at time 1 should be greater than never detecting.  Time 1: "
						+ atTime1 + ", Time1: " + atNever, atTime1 > atNever);
	}

	@Test
	public void notDetectedSetEqualsZeroReduction() {
		CascadeSet<String> incidents = new CascadeSet<String>(Collections.singleton(new Incident<String>(new DateTime(),
				new TestUser("user-a"), "undetected incident")));
		SensorPlacement<String> sensors = new SensorPlacement<String>(singleton(new TestUser("user-b")));
		IncidentDistribution dist = new IncidentDistribution() {
			@Override
			public <K extends Comparable<K>> double probability(Incident<K> incident) {
				return 1.0D;
			}

			@Override
			public <K extends Comparable<K>> double probability(IncidentCascade<K> cascade) {
				return 1.0D;
			}
		};
		assertEquals(0, Penalty.<String>detectionTime().penaltyReduction(dist, incidents,
				sensors));
	}

	@Test
	public void detectedCascadeReductionProportionalToIncidentProbability() {
		DateTime now = new DateTime();
		TestUser userA = new TestUser("user-a");
		CascadeSet<String> highlyProbable = new CascadeSet<String>(Collections.singleton(new Incident<String>(now, userA,
				"important incident")));
		CascadeSet<String> highlyUnlikely = new CascadeSet<String>(Collections.singleton(new Incident<String>(now, userA,
				"trivial incident")));
		SensorPlacement<String> sensors = new SensorPlacement<String>(singleton(userA));
		IncidentDistribution dist = new IncidentDistribution() {
			@Override
			public <K extends Comparable<K>> double probability(IncidentCascade<K> cascade) {
				if ("important incident".equals(cascade.getIdentifier())) {
					return 0.95D;
				}
				return 0.1D;
			}

			@Override
			public <K extends Comparable<K>> double probability(Incident<K> incident) {
				if ("important incident".equals(incident.getIdentifier())) {
					return 0.95D;
				}
				return 0.1D;
			}
		};
		assertGreaterThan(Penalty.<String>detectionTime().penaltyReduction(dist, highlyProbable,
				sensors), Penalty.<String>detectionTime().penaltyReduction(dist, highlyUnlikely,
				sensors));
	}

	@Test
	public void detectionLikelihoodWithHorizon() {
		Instant horizon = new DateTime().plusDays(2).toInstant();
		TestUser userA = new TestUser("user-a");
		TestUser userB = new TestUser("user-b");
		List<Incident<String>> is = new ArrayList<Incident<String>>();
		is.add(new Incident<String>(new DateTime().plusDays(1), userA,
				"incident"));
		is.add(new Incident<String>(new DateTime().plusDays(3), userB,
				"incident"));
		CascadeSet<String> incidents = new CascadeSet<String>(is);
		SensorPlacement<String> notDetected = new SensorPlacement<String>(singleton(userB));
		SensorPlacement<String> detected = new SensorPlacement<String>(singleton(userA));
		IncidentDistribution dist = new IncidentDistribution() {
			@Override
			public <K extends Comparable<K>> double probability(Incident<K> incident) {
				return 1.0D;
			}

			@Override
			public <K extends Comparable<K>> double probability(IncidentCascade<K> cascade) {
				return 1.0D;
			}
		};
		assertGreaterThan(Penalty.<String>detectionLikelihood(horizon).penaltyReduction(dist,
				incidents, detected), Penalty.<String>detectionLikelihood(horizon)
				.penaltyReduction(dist, incidents, notDetected));
	}
	
	@Test
	public void populationAffectedWithCascadeSet() {
		TestUser userA = new TestUser("user-a");
		TestUser userB = new TestUser("user-b");
		List<Incident<String>> is = new ArrayList<Incident<String>>();
		is.add(new Incident<String>(new DateTime().plusDays(1), userA,
				"incident"));
		is.add(new Incident<String>(new DateTime().plusDays(2), userB,
				"incident"));
		CascadeSet<String> cascades = new CascadeSet<String>(is);
		SensorPlacement<String> oneAffected = new SensorPlacement<String>(singleton(userA));
		SensorPlacement<String> twoAffected = new SensorPlacement<String>(singleton(userB));
		IncidentDistribution dist = new IncidentDistribution() {
			@Override
			public <K extends Comparable<K>> double probability(Incident<K> incident) {
				return 1D;
			}

			@Override
			public <K extends Comparable<K>> double probability(IncidentCascade<K> cascade) {
				return 1D;
			}
		};
		assertGreaterThan(Penalty.<String>populationAffected(cascades.iterator().next()).penaltyReduction(dist, cascades,
				oneAffected), Penalty.<String>populationAffected(cascades.iterator().next()).penaltyReduction(dist,
				cascades, twoAffected));
	}

	private <T extends Comparable<T>> void assertGreaterThan(T lhs, T rhs) {
		assertTrue("Expected " + lhs + " to be greater than " + rhs, lhs
				.compareTo(rhs) > 0);
	}

}
