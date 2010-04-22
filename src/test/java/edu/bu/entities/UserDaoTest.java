package edu.bu.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

public class UserDaoTest {
	
	@Test
	public void saveDeleteLoad() {
		UserDao dao = new UserDao();
		User user = User.createUser(1L, "username", 0);
		try {
			dao.save(user);
			User user0 = dao.get(user.getId());
			assertEquals(user, user0);
		} finally {
			dao.delete(user);
		}
		assertNull(dao.get(user.getId()));
	}
	
	@Test
	public void selectWhere() {
		UserDao dao = new UserDao();
		User user0 = User.createUser(0L, "user0", 0);
		User user1 = User.createUser(1L, "user1", 0);
		User user2 = User.createUser(2L, "user2", 0);
		User user3 = User.createUser(3L, "user3", 0);
		try {
			dao.save(user0, user1, user2, user3);
			List<User> results = dao.findWithIdGt(0L, 2);
			assertEquals(2, results.size());
			assertTrue(results.contains(user1));
			assertTrue(results.contains(user2));
		} finally {
			dao.delete(user0, user1, user2, user3);
		}
	}
	
	@Test
	public void getAll() {
		UserDao dao = new UserDao();
		User user0 = User.createUser(0L, "user0", 0);
		User user1 = User.createUser(1L, "user1", 0);
		try {
			dao.save(user0, user1);
			List<User> results = dao.getAll();
			
			assertTrue(results.size() >= 2);
			assertTrue(results.contains(user0));
			assertTrue(results.contains(user1));
		} finally {
			dao.delete(user0, user1);
		}
	}
	
	@Test
	public void getHashCount() {
		UserDao dao = new UserDao();
		HashDao hashDao = new HashDao();
		User user = User.createUser(0L, "user", 0);
		Status status0 = Status.createStatus(1L, user, "I love #hash", new DateTime(), true);
		Status status1 = Status.createStatus(2L, user, "seriously, I love #hash", new DateTime(), true);
		Hash hash = Hash.createHash("#hash", true, Arrays.asList(status0, status1));
		try {
			dao.save(user);
			hashDao.save(hash);
			
			assertEquals(2, dao.getHashCount(user));
		} finally {
			hashDao.delete(hash);
			dao.delete(user);
		}
		
	}

}
