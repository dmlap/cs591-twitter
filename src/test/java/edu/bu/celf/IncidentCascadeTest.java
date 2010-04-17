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
	
	@Test
	public void predecessorCount() {
		TwitterUser user0 = new TwitterUser("zero");
		TwitterUser user1 = new TwitterUser("one");
		TwitterUser user2 = new TwitterUser("two");
		TwitterUser user3 = new TwitterUser("three");
		TwitterUser user4 = new TwitterUser("four");
		IncidentCascade cascade = new IncidentCascade(
				new Incident(new DateTime(), user0, "i"), 
				new Incident(new DateTime().plusDays(1), user1, "i"),
				new Incident(new DateTime().plusDays(2), user2, "i"),
				new Incident(new DateTime().plusDays(3), user3, "i"));
		
		assertEquals(0, cascade.predecessorCount(user0));
		assertEquals(1, cascade.predecessorCount(user1));
		assertEquals(2, cascade.predecessorCount(user2));
		assertEquals(3, cascade.predecessorCount(user3));
		assertEquals(4, cascade.predecessorCount(user4));
	}

}
