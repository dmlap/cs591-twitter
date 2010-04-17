package edu.bu.entities;

import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

import edu.bu.entities.HibernateUtil.HibernateStatement;

public class LastIDDao implements Dao<LastID, String> {
	@Override
	public LastID get(final String key) {
		return HibernateUtil.doWithSession(new HibernateStatement<LastID>() {
			@Override
			public LastID run(Session session) {
				return (LastID) session.createCriteria(LastID.class).add(
						Restrictions.idEq(key)).uniqueResult();
			}
		});
	}

	@Override
	public void save(final LastID target, final LastID... targets) {
			HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.save(target);
				for(LastID t : targets) {
					session.save(t);
				}
				session.flush();
				return null;
			}
		});
	}

	@Override
	public void delete(final LastID target, final LastID... targets) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.delete(target);
				for(LastID t : targets) {
					session.delete(t);
				}
				return null;
			}
		});
	}
	
	@Override
	public void update(final LastID target, final LastID... targets) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.update(target);
				for(LastID t : targets) {
					session.update(t);
				}
				return null;
			}
		});
	}
}
