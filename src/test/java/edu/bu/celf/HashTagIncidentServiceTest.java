package edu.bu.celf;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Test;

import edu.bu.Incident;
import edu.bu.entities.Hash;
import edu.bu.entities.HashDao;
import edu.bu.entities.Status;
import edu.bu.entities.StatusDao;
import edu.bu.entities.User;
import edu.bu.entities.UserDao;

public class HashTagIncidentServiceTest {
	
	@Test
	public void getAllIncidents() {
		UserDao users = new UserDao();
		HashDao hashes = new HashDao();
		HashTagIncidentService htis = new HashTagIncidentService(hashes);
		User user = null;
		Hash hash = null;
		List<Status> statuses = new ArrayList<Status>();
		try {
			user = User.createUser(1L, "username", 1);
			users.save(user);
			statuses.add(Status.createStatus(1L, user, "i like #twitter", new DateTime(), true));
			hash = Hash.createHash("#twitter", statuses);
			hashes.save(hash);
			Set<Incident<Long>> incidents = htis.getAllIncidents();
			for(Incident<Long> incident : incidents) {
				if(hash.getHash().equals(incident.getIdentifier())) {
					return;
				}
			}
			fail("did not find all incidents");
		} finally {
			hashes.delete(hash);
			new StatusDao().delete(statuses.get(0));
			users.delete(user);
		}
	}

}
