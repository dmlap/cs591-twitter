/**
 * 
 */
package edu.bu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import edu.bu.celf.FixedCostAppraiser;
import edu.bu.celf.GreedySensorSelector;
import edu.bu.celf.HashTagIncidentService;
import edu.bu.celf.IncidentDistribution;
import edu.bu.celf.Penalty;
import edu.bu.celf.SensorEvaluator;
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
	private static final List<Long> LEADERS = Arrays.asList(56390304L,
			78481368L, 22029942L, 34406585L, 71034594L, 48447264L, 55601198L,
			62035633L, 65026943L, 27096548L);

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
		IncidentDistribution distribution = new IncidentDistribution() {
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
		};
		long maxPenalty = new DateTime().minusYears(1).getMillis();
		List<Pair<Double, Penalty<Long>>> penalties = new ArrayList<Pair<Double, Penalty<Long>>>();
		Pair<Double, Penalty<Long>> detectionTime = new Pair<Double, Penalty<Long>>(
				1.0D, Penalty.<Long> detectionTime(maxPenalty));
		Pair<Double, Penalty<Long>> starters = new Pair<Double, Penalty<Long>>(
				1161872521D, new Penalty<Long>(new SensorEvaluator<Long>() {
					@Override
					public long evaluate(Sensor<Long> sensor,
							Interval detectionInterval) {
						return LEADERS.contains(sensor.getId()) ? 1L : 0L;
					}
				}));
		penalties.add(detectionTime);
		penalties.add(starters);
		Set<User> allUsers = new HashSet<User>(userDao.getAll());
		CascadeSet<Long> cascades = new CascadeSet<Long>(incidents);
		Penalty<Long> penalty = Penalty.<Long> compose(penalties);
		GreedySensorSelector<Long> celf = new GreedySensorSelector<Long>(
				new FixedCostAppraiser<Long>(1), penalty,
				distribution);

		System.out.println("Running unit-cost CELF...");
		
		Set<User> users = celf.select(budget, allUsers, cascades);
		
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
		System.out.println("Benefit: " + penalty.penaltyReduction(distribution, cascades, users));
		System.out.println("\nDone.");
	}

}
