/**
 * 
 */
package edu.bu.celf;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.classic.Session;
import org.joda.time.DateTime;

import edu.bu.Incident;
import edu.bu.Sensor;
import edu.bu.entities.Dao;
import edu.bu.entities.Hash;
import edu.bu.entities.HashDao;
import edu.bu.entities.HibernateUtil;
import edu.bu.entities.HibernateUtil.HibernateStatement;

/**
 * A service for {@link Incident}s based on {@link Hash}es.
 * 
 * @author dml
 * 
 */
public class HashTagIncidentService {
	private final Dao<Hash, String> hashDao;

	/**
	 * Construct a new {@link HashTagIncidentService} that uses the specified {@link HashDao}
	 * @param hashDao
	 */
	public HashTagIncidentService(Dao<Hash, String> hashDao) {
		this.hashDao = hashDao;
	}
	
	/**
	 * Returns the {@link Set} of all {@link Hash} {@link Incident}s.
	 * 
	 * @return the {@link Set} of all {@link Hash} {@link Incident}s.
	 */
	public Set<Incident<Long>> getAllIncidents() {
		return HibernateUtil.doWithSession(new HibernateStatement<Set<Incident<Long>>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Set<Incident<Long>> run(Session session) {
				HashSet<Incident<Long>> results = new HashSet<Incident<Long>>();
				Iterator<Object[]> itr = session.createQuery("select s.statusDate, s.user, h.id from Hash h join h.statuses s").iterate();
				while(itr.hasNext()) {
					Object[] incident = itr.next();
					results.add(new Incident<Long>((DateTime) incident[0], (Sensor<Long>) incident[1], (String) incident[2]));
				}
				return results;
			}
		});
	}

}
