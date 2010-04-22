/**
 * 
 */
package edu.bu;

import java.util.HashSet;
import java.util.Set;

import edu.bu.celf.FixedCostAppraiser;
import edu.bu.celf.GreedySensorSelector;
import edu.bu.celf.HashTagIncidentService;
import edu.bu.celf.IncidentDistribution;
import edu.bu.celf.Penalty;
import edu.bu.entities.HashDao;
import edu.bu.entities.StatusDao;
import edu.bu.entities.User;
import edu.bu.entities.UserDao;


/**
 * Main class for running the CELF algorithm against a pre-populated database.
 * 
 * @author dml
 * 
 */
public class RunCelf {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int budget = 10;
		
		System.out.println("Initializing unit-cost CELF with budget " + budget + "...");
		
		UserDao userDao = new UserDao();
		StatusDao statusDao = new StatusDao();
		HashDao hashDao = new HashDao();
		HashTagIncidentService htis = new HashTagIncidentService();
		final Set<Incident<Long>> incidents = htis.getAllIncidents();
		GreedySensorSelector<Long> celf = new GreedySensorSelector<Long>(
				new FixedCostAppraiser<Long>(1),
				Penalty.<Long> detectionTime(), new IncidentDistribution() {
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
		
		System.out.println("Running unit-cost CELF...");
		
		Set<User> users = celf.select(budget, new HashSet<User>(
				userDao.getAll()), new CascadeSet<Long>(incidents));
		
		System.out.println("\nResults:");
		System.out.println("Number of users: " + userDao.getCount());
		System.out.println("Number of status messages: " + statusDao.getCount());
		System.out.println("Number of unique hashes: " + hashDao.getCount());
		System.out.println("Selected " + users.size() + " users");
		for (User user : users) {
			System.out.println(user.getName() + " (" + user.getId() + ")");
			System.out.println("  degree " + user.getDegree());
			System.out.println("  number of hashes: " + userDao.getHashCount(user));
		}
		System.out.println("\nDone.");
	}

}
