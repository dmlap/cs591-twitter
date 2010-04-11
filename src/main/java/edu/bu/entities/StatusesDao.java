package edu.bu.entities;

import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

import edu.bu.entities.HibernateUtil.HibernateStatement;

public class StatusesDao implements Dao<Statuses, Long> {

	@Override
	public Statuses get(final Long key) {
		return HibernateUtil.doWithSession(new HibernateStatement<Statuses>() {
			@Override
			public Statuses run(Session session) {
				return (Statuses) session.createCriteria(Statuses.class).add(
						Restrictions.idEq(key)).uniqueResult();
			}
		});
	}

	@Override
	public void save(final Statuses target, final Statuses... targets) {
			HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.save(target);
				for(Statuses t : targets) {
					session.save(t);
				}
				session.flush();
				return null;
			}
		});
	}

	@Override
	public void delete(final Statuses target, final Statuses... targets) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.delete(target);
				for(Statuses t : targets) {
					session.delete(t);
				}
				return null;
			}
		});
	}	
}