package edu.bu.entities;

import java.util.List;

import org.hibernate.classic.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import edu.bu.entities.HibernateUtil.HibernateStatement;

public class UserDao implements Dao<User, Long> {
	@Override
	public User get(final Long key) {
		return HibernateUtil.doWithSession(new HibernateStatement<User>() {
			@Override
			public User run(Session session) {
				return (User) session.createCriteria(User.class).add(
						Restrictions.idEq(key)).uniqueResult();
			}
		});
	}

	@Override
	public void save(final User user, final User... users) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.save(user);
				for (User u : users) {
					session.save(u);
				}
				return null;
			}
		});
	}

	@Override
	public void delete(final User user, final User... users) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.delete(user);
				for (User u : users) {
					session.delete(u);
				}
				return null;
			}
		});
	}
	
	@Override
	public void update(final User user, final User... users) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.update(user);
				for (User u : users) {
					session.update(u);
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
	public List<User> findWithIdGt(final long id, final int count) {
		return HibernateUtil
				.doWithSession(new HibernateStatement<List<User>>() {
					@SuppressWarnings("unchecked")
					@Override
					public List<User> run(Session session) {
						return session.createCriteria(User.class).add(
								Restrictions.gt("id", id)).addOrder(
								Order.asc("id")).setMaxResults(count).list();
					}
				});
	}

	/**
	 * Gets the highest user ID value in the table
	 * 
	 * @return A User object containing the ID
	 */
	public User findMaxId() {
		return HibernateUtil.doWithSession(new HibernateStatement<User>() {
			@Override
			public User run(Session session) {
				Long userId = (Long) session.createCriteria(User.class)
						.setProjection(Projections.max("id")).uniqueResult();
				return User.createUser(userId, "", 0);
			}
		});
	}
	
	/**
	 * Gets the total users in the table
	 * 
	 * @return A long with the total number of rows
	 */
	public Long getCount() {
		return HibernateUtil.doWithSession(new HibernateStatement<Long>() {
			@Override
			public Long run(Session session) {
				return (Long) session.createQuery("select count(*) from User").uniqueResult();
			}
		});
	}
	
	/**
	 * Gets the top ten users by degree
	 * 
	 * @return List of the 10 users
	 */
	public List<User> topTen() {
		return HibernateUtil
				.doWithSession(new HibernateStatement<List<User>>() {
					@SuppressWarnings("unchecked")
					@Override
					public List<User> run(Session session) {
						return session.createCriteria(User.class).addOrder(
								Order.desc("degree")).setMaxResults(10).list();
					}
				});
	}
	
	/**
	 * Returns the number of {@link Status statuses} the specified {@link User}
	 * has posted with a {@link Hash} in it.
	 * 
	 * @param user
	 *            - the {@link User} to query
	 * @return the number of {@link Status statuses} the specified {@link User}
	 *         has posted with a {@link Hash} in it.
	 */
	public int getHashCount(final User user) {
		return HibernateUtil.doWithSession(new HibernateStatement<Integer>() {
			@Override
			public Integer run(Session session) {
				return (Integer) session.createCriteria(Hash.class)
					.setProjection(Projections.rowCount())
					.createCriteria("statuses")
						.add(Restrictions.eq("user.id", user.getId()))
					.uniqueResult();
			}
		});
	}

	/**
	 * Returns all {@link User}s
	 * 
	 * @return all {@link User}s
	 */
	public List<User> getAll() {
		return HibernateUtil
				.doWithSession(new HibernateStatement<List<User>>() {
					@Override
					@SuppressWarnings("unchecked")
					public List<User> run(Session session) {
						return session.createCriteria(User.class).list();
					}
				});
	}
}
