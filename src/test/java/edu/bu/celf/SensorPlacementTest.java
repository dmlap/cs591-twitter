package edu.bu.celf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import edu.bu.SimpleUser;

public class SensorPlacementTest {
	
	@Test
	public void containsTrue() {
		SimpleUser s = new SimpleUser("user");
		assertTrue(new SensorPlacement<String>(Collections.singleton(s)).contains(s));
	}

	@Test
	public void containsFalse() {
		SimpleUser s = new SimpleUser("user");
		assertFalse(new SensorPlacement<String>(Collections.singleton(new SimpleUser("not-user"))).contains(s));
	}

}
