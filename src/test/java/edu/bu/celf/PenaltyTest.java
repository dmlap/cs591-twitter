package edu.bu.celf;

import static edu.bu.celf.Penalty.INTERVAL_PENALTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;

import edu.bu.CascadeSet;
import edu.bu.Incident;
import edu.bu.IncidentCascade;
import edu.bu.TwitterUser;

public class PenaltyTest {

	@Test
	public void notDetectedIncidentEqualsZeroReduction() {
		Incident incident = new Incident(new DateTime(), new TwitterUser(
				"user-a"), "undetected-incident");
		SensorPlacement sensors = new SensorPlacement(new TwitterUser("user-b"));
		assertEquals(0, INTERVAL_PENALTY.penaltyReduction(IncidentCascade.singleton(incident), sensors));
	}
	
	@Test
	public void detectedIncidentReductionProportionalToDetectionTime() {
		TwitterUser userA = new TwitterUser("userA");
		TwitterUser userB = new TwitterUser("userB");
		TwitterUser userC = new TwitterUser("userC");
		SensorPlacement onlyA = new SensorPlacement(userA);
		SensorPlacement onlyB = new SensorPlacement(userB);
		SensorPlacement onlyC = new SensorPlacement(userC);
		DateTime time0 = new DateTime();
		DateTime time1 = new DateTime(time0.getMillis() + 1);
		
		IncidentCascade cascade = new IncidentCascade(new Incident(time0, userA, "id"), new Incident(time1, userB, "id"));

		long atTime0 = INTERVAL_PENALTY.penaltyReduction(cascade, onlyA);
		long atTime1 = INTERVAL_PENALTY.penaltyReduction(cascade, onlyB);
		long atNever = INTERVAL_PENALTY.penaltyReduction(cascade, onlyC);

		assertTrue("Penalty reduction for detecting at time 0 should be greater than detection at time 1.  Time 0: " + atTime0 + ", Time1: " + atTime1, atTime0 > atTime1);
		assertTrue("Penalty reduction for detecting at time 0 should be greater than never detecting.  Time 0: " + atTime0 + ", never: " + atNever, atTime0 > atNever);
		assertTrue("Penalty reduction for detecting at time 1 should be greater than never detecting.  Time 1: " + atTime1 + ", Time1: " + atNever, atTime1 > atNever);
	}
	
	@Test
	public void notDetectedSetEqualsZeroReduction() {
		CascadeSet incidents = new CascadeSet(new Incident(new DateTime(),
				new TwitterUser("user-a"), "undetected incident"));
		SensorPlacement sensors = new SensorPlacement(new TwitterUser("user-b"));
		IncidentDistribution dist = new IncidentDistribution() {
			@Override
			public double probability(Incident incident) {
				return 1.0D;
			}
			@Override
			public double probability(IncidentCascade cascade) {
				return 1.0D;
			}
		};
		assertEquals(0, INTERVAL_PENALTY.penaltyReduction(dist, incidents, sensors));
	}
	
	@Test
	public void detectedCascadeReductionProportionalToIncidentProbability() {
		DateTime now = new DateTime();
		TwitterUser userA = new TwitterUser("user-a");
		CascadeSet highlyProbable = new CascadeSet(new Incident(now, userA, "important incident"));
		CascadeSet highlyUnlikely = new CascadeSet(new Incident(now, userA, "trivial incident"));
		SensorPlacement sensors = new SensorPlacement(userA);
		IncidentDistribution dist = new IncidentDistribution() {
			@Override
			public double probability(IncidentCascade cascade) {
				if("important incident".equals(cascade.getIdentifier())) {
					return 0.95D;
				}
				return 0.1D;
			}
			@Override
			public double probability(Incident incident) {
				if("important incident".equals(incident.getIdentifier())) {
					return 0.95D;
				}
				return 0.1D;
			}
		};
		assertGreaterThan(INTERVAL_PENALTY.penaltyReduction(dist,
				highlyProbable, sensors), INTERVAL_PENALTY.penaltyReduction(
				dist, highlyUnlikely, sensors));
	}
	
	private <T extends Comparable<T>> void assertGreaterThan(T lhs, T rhs) {
		assertTrue("Expected " + lhs + " to be greater than " + rhs, lhs.compareTo(rhs) > 0);
	}

}
