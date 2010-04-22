package edu.bu;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.bu.celf.FixedCostAppraiser;
import edu.bu.celf.GreedySensorSelector;
import edu.bu.celf.HashTagIncidentService;
import edu.bu.celf.IncidentDistribution;
import edu.bu.celf.Penalty;
import edu.bu.entities.Hash;
import edu.bu.entities.HashDao;
import edu.bu.entities.Status;
import edu.bu.entities.User;
import edu.bu.entities.UserDao;

public class CELFTest {
	
	private UserDao users = new UserDao();
	private HashDao hashes = new HashDao();
	private HashTagIncidentService htis = new HashTagIncidentService();
	private User user = User.createUser(1L, "username", 1);
	private Status status = Status.createStatus(1L, user, "i like #twitter", new DateTime(), true);
	private Hash hash = Hash.createHash("#twitter", false, Collections.singletonList(status));
	
	@Before
	public void initDb() {
		users.save(user);
		hashes.save(hash);
	}
	
	@After
	public void shutdownDb() {
		hashes.delete(hash);
		users.delete(user);
	}
	
	@Test
	public void selectOne() {
		final Set<Incident<Long>> incidents = htis.getAllIncidents();
		GreedySensorSelector<Long> celf = new GreedySensorSelector<Long>(
				new FixedCostAppraiser<Long>(1), Penalty
						.<Long> detectionTime(),
				new IncidentDistribution() {
					@Override
					public <K extends Comparable<K>> double probability(
							IncidentCascade<K> cascade) {
						return 1 / incidents.size();
					}

					@Override
					public <K extends Comparable<K>> double probability(
							Incident<K> incident) {
						return 1 / incidents.size();
					}
				});
		Set<Sensor<Long>> results = celf.select(1, Collections.<Sensor<Long>> singleton(user),
				new CascadeSet<Long>(incidents));
		
		assertEquals(1, results.size());
		assertEquals(user, results.toArray()[0]);
	}
	
	@Test
	public void selectNone() {
		final Set<Incident<Long>> incidents = htis.getAllIncidents();
		GreedySensorSelector<Long> celf = new GreedySensorSelector<Long>(
				new FixedCostAppraiser<Long>(1), Penalty
						.<Long> detectionTime(),
				new IncidentDistribution() {
					@Override
					public <K extends Comparable<K>> double probability(
							IncidentCascade<K> cascade) {
						return 1 / incidents.size();
					}

					@Override
					public <K extends Comparable<K>> double probability(
							Incident<K> incident) {
						return 1 / incidents.size();
					}
				});
		Set<Sensor<Long>> results = celf.select(0, Collections.<Sensor<Long>> singleton(user),
				new CascadeSet<Long>(incidents));
		
		assertEquals(0, results.size());
	}

}
