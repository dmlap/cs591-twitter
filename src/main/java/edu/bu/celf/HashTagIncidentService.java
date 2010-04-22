/**
 * 
 */
package edu.bu.celf;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.classic.Session;
import org.hibernate.criterion.Projections;
import org.joda.time.DateTime;

import edu.bu.Incident;
import edu.bu.Sensor;
import edu.bu.entities.Hash;
import edu.bu.entities.HibernateUtil;
import edu.bu.entities.HibernateUtil.HibernateStatement;

/**
 * A service for {@link Incident}s based on {@link Hash}es.
 * 
 * @author dml
 * 
 */
public class HashTagIncidentService {
	/**
	 * Returns the {@link Set} of all {@link Hash} {@link Incident}s.
	 * 
	 * @return the {@link Set} of all {@link Hash} {@link Incident}s.
	 */
	public Set<Incident<Long>> getAllIncidents() {
		return HibernateUtil.doWithSession(new HibernateStatement<Set<Incident<Long>>>() {
			@Override
			public Set<Incident<Long>> run(Session session) {
				HashSet<Incident<Long>> results = new HashSet<Incident<Long>>();
				List list = session.createCriteria(Hash.class, "hash")
					.createCriteria("statuses", "status")
							.setProjection(Projections.projectionList()
									.add(Projections.property("status.statusDate"))
									.add(Projections.property("status.user"))
									.add(Projections.property("hash.hash")))
					.list();//createQuery("select s.statusDate, s.user, h.id from Hash h join h.statuses s").iterate();
				Iterator<Object[]> itr = list.iterator();
				while(itr.hasNext()) {
					Object[] incident = itr.next();
					results.add(new Incident<Long>((DateTime) incident[0], (Sensor<Long>) incident[1], (String) incident[2]));
				}
				return results;
			}
		});
	}

}
