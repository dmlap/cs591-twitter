package edu.bu.celf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import edu.bu.TestUser;

public class SensorPlacementTest {
	
	@Test
	public void containsTrue() {
		TestUser s = new TestUser("user");
		assertTrue(new SensorPlacement<String>(Collections.singleton(s)).contains(s));
	}

	@Test
	public void containsFalse() {
		TestUser s = new TestUser("user");
		assertFalse(new SensorPlacement<String>(Collections.singleton(new TestUser("not-user"))).contains(s));
	}

}
