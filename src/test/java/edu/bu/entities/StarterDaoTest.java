package edu.bu.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class StarterDaoTest {
	@Test
	public void saveDeleteLoad() {
		StarterDao dao = new StarterDao();
		Starter starter = Starter.createStarter(1L, 5);
		try {
			dao.save(starter);
			Starter starter0 = dao.get(starter.getId());
			assertEquals(starter, starter0);
		} finally {
			dao.delete(starter);
		}
		assertNull(dao.get(starter.getId()));
	}
}
