package edu.bu.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class UserDaoTest {
	
	@Test
	public void saveDeleteLoad() {
		UserDao dao = new UserDao();
		Users user = Users.createUser(1L, "username", 0);
		dao.save(user);
		Users user0 = dao.get(user.getId());
		assertEquals(user, user0);
		
		dao.delete(user);
		assertNull(dao.get(user.getId()));
	}

}
