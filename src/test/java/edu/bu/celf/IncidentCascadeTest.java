package edu.bu.celf;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;

import edu.bu.Incident;
import edu.bu.IncidentCascade;
import edu.bu.TwitterUser;

public class IncidentCascadeTest {

	@Test
	public void getSource() {
		DateTime time0 = new DateTime();
		TwitterUser userA = new TwitterUser("a");
		IncidentCascade cascade = new IncidentCascade(new Incident(time0, userA, "tag"),
				new Incident(new DateTime(time0.getMillis() + 10L),
						new TwitterUser("b"), "tag"));
		assertEquals(time0, cascade.getSource().getDateTime());
		assertEquals(userA, cascade.getSource().getSensor());
		assertEquals(cascade.getIdentifier(), cascade.getSource().getIdentifier());
	}
	
	@Test
	public void detectionDelay() {
		DateTime start = new DateTime();
		DateTime detected = new DateTime(start.getMillis() + 100L);
		Interval interval = new Interval(start, detected);
		TwitterUser user = new TwitterUser("detector");
		IncidentCascade cascade = new IncidentCascade(new Incident(start, new TwitterUser("start"), "i"), new Incident(detected, user, "i"));
		assertEquals(interval, cascade.detectionDelay(user));
	}

}
