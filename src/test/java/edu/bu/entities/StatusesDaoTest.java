package edu.bu.entities;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Test;

public class StatusesDaoTest {
	@AfterClass
	public static void resetTransactions() {
		HibernateUtil.reset();
	}
	
	@Test
	public void saveLoad() {
		StatusDao dao = new StatusDao();
		User user = User.createUser(1L, "name", 0);
		UserDao userDao = new UserDao();
		Status status = Status.createStatus(1L, user, "status", new DateTime(
				2010, 1, 1, 12, 0, 0, 0), false);
		try {
			userDao.save(user);
			dao.save(status);
			Status status0 = dao.get(status.getId());
			assertEquals(status, status0);
		} finally {
			dao.delete(status);
			userDao.delete(user);
		}
	}
}
