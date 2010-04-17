package edu.bu;

import java.util.Collections;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import edu.bu.entities.Hash;
import edu.bu.entities.HashDao;
import edu.bu.entities.Status;
import edu.bu.entities.StatusDao;
import edu.bu.entities.User;
import edu.bu.entities.UserDao;

public class CELFTest {
	
	@Test
	@Ignore("not implemented")
	public void integrationTest() {
		UserDao users = new UserDao();
		HashDao hashes = new HashDao();
		StatusDao statuses = new StatusDao();
		User user = User.createUser(1L, "username", 1);
		Status status = Status.createStatus(1L, user, "i like #twitter", new DateTime(), true);
		Hash hash = Hash.createHash("#twitter", Collections.singletonList(status));
		try {
			statuses.save(status);
			hashes.save(hash);
			users.save(user);
		} finally {
			statuses.delete(status);
			hashes.delete(hash);
			users.delete(user);
		}
	}

}
