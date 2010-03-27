package edu.bu.celf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.bu.TwitterUser;

public class SensorPlacementTest {
	
	@Test
	public void containsTrue() {
		TwitterUser s = new TwitterUser("user");
		assertTrue(new SensorPlacement(s).contains(s));
	}

	@Test
	public void containsFalse() {
		TwitterUser s = new TwitterUser("user");
		assertFalse(new SensorPlacement(new TwitterUser("not-user")).contains(s));
	}

}
