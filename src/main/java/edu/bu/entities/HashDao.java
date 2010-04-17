package edu.bu.entities;

import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

import edu.bu.entities.HibernateUtil.HibernateStatement;

public class HashDao implements Dao<Hash, String>{
	@Override
	public Hash get(final String key) {
		return HibernateUtil.doWithSession(new HibernateStatement<Hash>() {
			@Override
			public Hash run(Session session) {
				return (Hash) session.createCriteria(Hash.class).add(
						Restrictions.idEq(key)).uniqueResult();
			}
		});
	}

	@Override
	public void save(final Hash target, final Hash... targets) {
			HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.save(target);
				for(Hash t : targets) {
					session.save(t);
				}
				session.flush();
				return null;
			}
		});
	}

	@Override
	public void delete(final Hash target, final Hash... targets) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.delete(target);
				for(Hash t : targets) {
					session.delete(t);
				}
				return null;
			}
		});
	}
	
	@Override
	public void update(final Hash target, final Hash... targets) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.update(target);
				for(Hash t : targets) {
					session.update(t);
				}
				return null;
			}
		});
	}
}
