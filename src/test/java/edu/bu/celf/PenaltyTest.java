package edu.bu.celf;

import org.joda.time.DateTime;
import org.junit.Test;

import edu.bu.Incident;
import edu.bu.IncidentSet;
import edu.bu.TwitterUser;

public class PenaltyTest {
	
	@Test
	public void notDetectedEqualsMaxPenalty() {
		IncidentSet incidents = new IncidentSet(new Incident(new DateTime(),
				new TwitterUser("user-a"), "undetected incident"));
		SensorPlacement sensors = new SensorPlacement(new TwitterUser("user-b"));
	}

}
