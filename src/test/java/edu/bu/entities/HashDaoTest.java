package edu.bu.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.junit.Test;

public class HashDaoTest {
	@Test
	public void saveDeleteLoad() {
		HashDao dao = new HashDao();
		Hash hash = Hash.createHash("Test", false, new ArrayList<Status>());
		try {
			dao.save(hash);
			Hash hash0 = dao.get(hash.getHash());
			assertEquals(hash, hash0);
		} finally {
			dao.delete(hash);
		}
		assertNull(dao.get(hash.getHash()));
	}
}
