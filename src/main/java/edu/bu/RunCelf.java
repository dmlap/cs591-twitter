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
		
		Set<User> results = celf.select(budget, new HashSet<User>(
				userDao.getAll()), new CascadeSet<Long>(incidents));
		
		System.out.println("\nResults:");
		System.out.println("Selected " + results.size() + " users");
		for (User result : results) {
			System.out.println(result.getName() + " (" + result.getId() + ")");
			System.out.println("  degree " + result.getDegree());
		}
		System.out.println("\nDone.");
	}

}
