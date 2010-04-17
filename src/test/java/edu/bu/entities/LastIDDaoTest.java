package edu.bu.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class LastIDDaoTest {
	@Test
	public void saveDeleteLoad() {
		LastIDDao dao = new LastIDDao();
		LastID lastId = LastID.createLastID(1L, "UserID");
		try {
			dao.save(lastId);
			LastID lastId0 = dao.get(lastId.getType());
			assertEquals(lastId, lastId0);
		} finally {
			dao.delete(lastId);
		}
		assertNull(dao.get(lastId.getType()));
	}
}
