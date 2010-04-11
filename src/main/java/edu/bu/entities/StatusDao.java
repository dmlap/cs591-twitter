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
	public void save(final Status target, final Status... targets) {
			HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.save(target);
				for(Status t : targets) {
					session.save(t);
				}
				session.flush();
				return null;
			}
		});
	}

	@Override
	public void delete(final Status target, final Status... targets) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.delete(target);
				for(Status t : targets) {
					session.delete(t);
				}
				return null;
			}
		});
	}	
}