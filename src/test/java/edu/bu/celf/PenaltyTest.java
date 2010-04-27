package edu.bu.celf;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.junit.Ignore;
import org.junit.Test;

import edu.bu.CascadeSet;
import edu.bu.Incident;
import edu.bu.IncidentCascade;
import edu.bu.Pair;
import edu.bu.SimpleUser;

public class PenaltyTest {

	@Test
	public void notDetectedIncidentEqualsZeroReduction() {
		Incident<String> incident = new Incident<String>(new DateTime(), new SimpleUser(
				"user-a"), "undetected-incident");
		SensorPlacement<String> sensors = new SensorPlacement<String>(singleton(new SimpleUser("user-b")));
		assertEquals(0, Penalty.<String>detectionTime(Long.MAX_VALUE).penaltyReduction(IncidentCascade
				.singleton(incident), sensors));
	}

	@Test
	public void detectedIncidentReductionProportionalToDetectionTime() {
		SimpleUser userA = new SimpleUser("userA");
		SimpleUser userB = new SimpleUser("userB");
		SimpleUser userC = new SimpleUser("userC");
		SensorPlacement<String> onlyA = new SensorPlacement<String>(singleton(userA));
		SensorPlacement<String> onlyB = new SensorPlacement<String>(singleton(userB));
		SensorPlacement<String> onlyC = new SensorPlacement<String>(singleton(userC));
		DateTime time0 = new DateTime();
		DateTime time1 = time0.plusDays(1);

		Set<Incident<String>> incidents = new HashSet<Incident<String>>();
		incidents.add(new Incident<String>(time0,
				userA, "id"));
		incidents.add(new Incident<String>(time1, userB, "id"));
		IncidentCascade<String> cascade = new IncidentCascade<String>("id", incidents);

		long atTime0 = Penalty.<String>detectionTime(Long.MAX_VALUE).penaltyReduction(cascade, onlyA);
		long atTime1 = Penalty.<String>detectionTime(Long.MAX_VALUE).penaltyReduction(cascade, onlyB);
		long atNever = Penalty.<String>detectionTime(Long.MAX_VALUE).penaltyReduction(cascade, onlyC);

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
				new SimpleUser("user-a"), "undetected incident")));
		SensorPlacement<String> sensors = new SensorPlacement<String>(singleton(new SimpleUser("user-b")));
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
		assertEquals(0, Penalty.<String>detectionTime(Long.MAX_VALUE).penaltyReduction(dist, incidents,
				sensors));
	}

	@Test
	@Ignore
	public void detectedCascadeReductionProportionalToIncidentProbability() {
		DateTime now = new DateTime();
		SimpleUser userA = new SimpleUser("user-a");
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
		assertGreaterThan(Penalty.<String>detectionTime(Long.MAX_VALUE).penaltyReduction(dist, highlyProbable,
				sensors), Penalty.<String>detectionTime(Long.MAX_VALUE).penaltyReduction(dist, highlyUnlikely,
				sensors));
	}

	@Test
	public void detectionLikelihoodWithHorizon() {
		Instant horizon = new DateTime().plusDays(2).toInstant();
		SimpleUser userA = new SimpleUser("user-a");
		SimpleUser userB = new SimpleUser("user-b");
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
		SimpleUser userA = new SimpleUser("user-a");
		SimpleUser userB = new SimpleUser("user-b");
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
		assertGreaterThan(Penalty.<String>populationAffected(2, cascades.iterator().next()).penaltyReduction(dist, cascades,
				oneAffected), Penalty.<String>populationAffected(2, cascades.iterator().next()).penaltyReduction(dist,
				cascades, twoAffected));
	}
	
	@Test
	public void noSensorsIsZeroPenaltyReduction() {
		SimpleUser userA = new SimpleUser("user-a");
		SimpleUser userB = new SimpleUser("user-b");
		List<Incident<String>> is = new ArrayList<Incident<String>>();
		is.add(new Incident<String>(new DateTime().plusDays(1), userA,
				"incident"));
		is.add(new Incident<String>(new DateTime().plusDays(2), userB,
				"incident"));
		CascadeSet<String> cascades = new CascadeSet<String>(is);
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
		assertEquals(0, Penalty.<String> detectionTime(Long.MAX_VALUE).penaltyReduction(dist,
				cascades, Collections.<SimpleUser> emptySet()));
		assertEquals(0, Penalty.<String> populationAffected(2, 
				new IncidentCascade<String>("incident",
						new HashSet<Incident<String>>(is))).penaltyReduction(
				dist, cascades, Collections.<SimpleUser> emptySet()));
		assertEquals(0, Penalty.<String> detectionLikelihood(
				new DateTime().plusDays(2).toInstant()).penaltyReduction(dist,
				cascades, Collections.<SimpleUser> emptySet()));
	}
	
	@Test
	public void multipleCascadesPenaltyReduction() {
		SimpleUser userA = new SimpleUser("user-a");
		SimpleUser userB = new SimpleUser("user-b");
		SimpleUser userC = new SimpleUser("user-c");
		SimpleUser userD = new SimpleUser("user-d");
		List<Incident<String>> incidents = new ArrayList<Incident<String>>();
		incidents.add(new Incident<String>(new DateTime(), userA, "i0"));
		incidents.add(new Incident<String>(new DateTime().plus(1L), userB, "i0"));
		incidents.add(new Incident<String>(new DateTime(), userC, "i1"));
		incidents.add(new Incident<String>(new DateTime().plus(1L), userD, "i1"));
		CascadeSet<String> cascades = new CascadeSet<String>(incidents);
		IncidentDistribution distribution = new IncidentDistribution() {
			@Override
			public <K extends Comparable<K>> double probability(
					IncidentCascade<K> cascade) {
				return 1D;
			}
			@Override
			public <K extends Comparable<K>> double probability(Incident<K> incident) {
				return 1D;
			}
		};
		long maxValue = 10000L;

		long noSensorsBenefit = Penalty.<String> detectionTime(maxValue).penaltyReduction(
				distribution, cascades, Collections.<SimpleUser> emptySet());
		assertEquals(0, noSensorsBenefit);
		long withBBenefit = Penalty.<String> detectionTime(maxValue).penaltyReduction(
				distribution, cascades, Collections.singleton(userB));
		assertGreaterThan(withBBenefit, noSensorsBenefit);
		long withABenefit = Penalty.<String> detectionTime(maxValue).penaltyReduction(
				distribution, cascades, Collections.singleton(userA));
		assertGreaterThan(withABenefit, withBBenefit);
		
		long withABBenefit = Penalty.<String> detectionTime(maxValue).penaltyReduction(
				distribution, cascades, new HashSet<SimpleUser>(Arrays.asList(userA, userB)));
		assertEquals(withABenefit, withABBenefit);
		long withACBenefit = Penalty.<String> detectionTime(maxValue).penaltyReduction(
				distribution, cascades, new HashSet<SimpleUser>(Arrays.asList(userA, userC)));
		assertGreaterThan(withACBenefit, withABenefit);
		long withADBenefit = Penalty.<String> detectionTime(maxValue).penaltyReduction(
				distribution, cascades, new HashSet<SimpleUser>(Arrays.asList(userA, userD)));
		assertGreaterThan(withACBenefit, withADBenefit);
	}
	
	@Test
	public void compoundPenalty() {
		SimpleUser user = new SimpleUser("a");
		IncidentDistribution distribution = new IncidentDistribution() {

			@Override
			public <K extends Comparable<K>> double probability(
					IncidentCascade<K> cascade) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public <K extends Comparable<K>> double probability(
					Incident<K> incident) {
				// TODO Auto-generated method stub
				return 0;
			}
		};
		CascadeSet<String> cascades = new CascadeSet<String>(Collections.singleton(new Incident<String>(new DateTime(), user, "i")));
		List<Pair<Double, Penalty<String>>> penalties = new ArrayList<Pair<Double,Penalty<String>>>();
		penalties.add(new Pair<Double, Penalty<String>>(1D, Penalty
				.populationAffected(1, cascades.iterator().next())));
		penalties.add(new Pair<Double, Penalty<String>>(1D, Penalty
				.<String> detectionTime(1L)));
		Penalty<String> penalty = Penalty.<String> compose(penalties);

		assertEquals(2, penalty.penaltyReduction(distribution, cascades, Collections.singleton(user)));
	}

	private <T extends Comparable<T>> void assertGreaterThan(T lhs, T rhs) {
		assertTrue("Expected " + lhs + " to be greater than " + rhs, lhs
				.compareTo(rhs) > 0);
	}

}
