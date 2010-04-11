package edu.bu.entities;

import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

import edu.bu.entities.HibernateUtil.HibernateStatement;

public class StatusDao implements Dao<Status, Long> {

	@Override
	public Status get(final Long key) {
		return HibernateUtil.doWithSession(new HibernateStatement<Status>() {
			@Override
			public Status run(Session session) {
				return (Status) session.createCriteria(Status.class).add(
						Restrictions.idEq(key)).uniqueResult();
			}
		});
	}

	@Override
	public void save(final Status target) {
			HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.save(target);
				session.flush();
				return null;
			}
		});
	}	
}