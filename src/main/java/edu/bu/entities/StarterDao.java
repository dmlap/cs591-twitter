package edu.bu.entities;

import java.util.List;

import org.hibernate.classic.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import edu.bu.entities.HibernateUtil.HibernateStatement;

public class StarterDao implements Dao<Starter, Long>{
	@Override
	public Starter get(final Long userId) {
		return HibernateUtil.doWithSession(new HibernateStatement<Starter>() {
			@Override
			public Starter run(Session session) {
				return (Starter) session.createCriteria(Starter.class).add(
						Restrictions.idEq(userId)).uniqueResult();
			}
		});
	}

	@Override
	public void save(final Starter starter, final Starter... starters) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.save(starter);
				for (Starter s : starters) {
					session.save(s);
				}
				return null;
			}
		});
	}

	@Override
	public void delete(final Starter starter, final Starter... starters) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.delete(starter);
				for (Starter s : starters) {
					session.delete(s);
				}
				return null;
			}
		});
	}
	
	@Override
	public void update(final Starter starter, final Starter... starters) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.update(starter);
				for (Starter s : starters) {
					session.update(s);
				}
				return null;
			}
		});
	}
	
	/**
	 * Gets the top starters from the database
	 * 
	 * @param count
	 * 			- The number of starters to pull
	 * @return The list of starters
	 */
	public List<Starter> getTopStarters(final int count) {
		return HibernateUtil
				.doWithSession(new HibernateStatement<List<Starter>>() {
					@SuppressWarnings("unchecked")
					@Override
					public List<Starter> run(Session session) {
						return session.createCriteria(Starter.class)
							.addOrder(Order.desc("score"))
							.setMaxResults(count).list();
					}
				});
	}
	
	/**
	 * Delete all starters from the table
	 */
	public void deleteAll() {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.createQuery("delete Starter").executeUpdate();
				return null;
			}
		});
	}
}
