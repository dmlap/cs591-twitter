package edu.bu.entities;

import java.util.List;

import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

import edu.bu.entities.HibernateUtil.HibernateStatement;

public class UserDao implements Dao<Users, Long> {

	@Override
	public Users get(final Long key) {
		return HibernateUtil.doWithSession(new HibernateStatement<Users>() {
			@Override
			public Users run(Session session) {
				return (Users) session.createCriteria(Users.class).add(
						Restrictions.idEq(key)).uniqueResult();
			}
		});
	}

	@Override
	public void save(final Users user, final Users... users) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.save(user);
				for(Users u : users) {
					session.save(u);
				}
				return null;
			}
		});
	}

	@Override
	public void delete(final Users user, final Users... users) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.delete(user);
				for(Users u : users) {
					session.delete(u);
				}
				return null;
			}
		});
	}

	/**
	 * Returns all {@link Users} with an id greater than the specified value up
	 * to a specified number of results.
	 * 
	 * @param id
	 *            - the lower bound identifier
	 * @param count
	 *            - the maximum number of results to return.
	 * @return up to <code>count</code> {@link Users}, all with
	 *         {@link Users#getId() ids} greater than <code>id</code>
	 */
	public List<Users> findWithIdGt(final long id, final int count) {
		return HibernateUtil
				.doWithSession(new HibernateStatement<List<Users>>() {
					@SuppressWarnings("unchecked")
					@Override
					public List<Users> run(Session session) {
						return session.createCriteria(Users.class).add(
								Restrictions.gt("id", id)).setMaxResults(count)
								.list();
					}
				});
	}
	
}
