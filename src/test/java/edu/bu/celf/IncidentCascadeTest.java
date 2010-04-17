package edu.bu.celf;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;

import edu.bu.Incident;
import edu.bu.IncidentCascade;
import edu.bu.SimpleUser;

public class IncidentCascadeTest {

	@Test
	public void getSource() {
		DateTime time0 = new DateTime();
		SimpleUser userA = new SimpleUser("a");
		HashSet<Incident<String>> incidents = new HashSet<Incident<String>>();
		incidents.add(new Incident<String>(time0, userA, "tag"));
		incidents.add(new Incident<String>(
				new DateTime(time0.getMillis() + 10L), new SimpleUser("b"),
				"tag"));
		IncidentCascade<String> cascade = new IncidentCascade<String>("tag",
				incidents);
		assertEquals(time0, cascade.getSource().getDateTime());
		assertEquals(userA, cascade.getSource().getSensor());
		assertEquals(cascade.getIdentifier(), cascade.getSource().getIdentifier());
	}
	
	@Test
	public void detectionDelay() {
		DateTime start = new DateTime();
		DateTime detected = new DateTime(start.getMillis() + 100L);
		Interval interval = new Interval(start, detected);
		SimpleUser user = new SimpleUser("detector");
		HashSet<Incident<String>> incidents = new HashSet<Incident<String>>();
		incidents.add(new Incident<String>(start, new SimpleUser("start"), "i"));
		incidents.add(new Incident<String>(detected, user, "i"));
		IncidentCascade<String> cascade = new IncidentCascade<String>("i", incidents);
		assertEquals(interval, cascade.detectionDelay(user));
	}
	
	@Test
	public void predecessorCount() {
		SimpleUser user0 = new SimpleUser("zero");
		SimpleUser user1 = new SimpleUser("one");
		SimpleUser user2 = new SimpleUser("two");
		SimpleUser user3 = new SimpleUser("three");
		SimpleUser user4 = new SimpleUser("four");
		HashSet<Incident<String>> incidents = new HashSet<Incident<String>>();
		incidents.add(new Incident<String>(new DateTime(), user0, "i"));
		incidents.add(new Incident<String>(new DateTime().plusDays(1), user1, "i"));
		incidents.add(new Incident<String>(new DateTime().plusDays(2), user2, "i"));
		incidents.add(new Incident<String>(new DateTime().plusDays(3), user3, "i"));
		IncidentCascade<String> cascade = new IncidentCascade<String>("i", incidents);
		
		assertEquals(0, cascade.predecessorCount(user0));
		assertEquals(1, cascade.predecessorCount(user1));
		assertEquals(2, cascade.predecessorCount(user2));
		assertEquals(3, cascade.predecessorCount(user3));
		assertEquals(4, cascade.predecessorCount(user4));
	}

}
