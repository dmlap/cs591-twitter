package edu.bu.entities;

import java.util.List;

import org.hibernate.classic.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import edu.bu.entities.HibernateUtil.HibernateStatement;

public class StatusDao implements Dao<Status, Long> {

	@Override
	public Status get(final Long key) {
		return HibernateUtil.doWithSession(new HibernateStatement<Status>() {
			@Override
			public Status run(Session session) {
				Status status = (Status) session.createCriteria(Status.class).add(
						Restrictions.idEq(key)).uniqueResult();
				session.flush();
				return status;
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
				session.evict(target);
				session.delete(target);
				for(Status t : targets) {
					session.evict(t);
					session.delete(t);
				}
				session.flush();
				return null;
			}
		});
	}
	
	@Override
	public void update(final Status target, final Status... targets) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.update(target);
				for(Status t : targets) {
					session.update(t);
				}
				session.flush();
				return null;
			}
		});
	}
	
	/**
	 * Gets the newest status ID for the specified user
	 * 
	 * @param id
	 * 			- The ID of the user to pull for
	 * @return The Status containing the max status ID
	 */
	public Status getMaxStatusForUser(final long id) {
		return HibernateUtil
				.doWithSession(new HibernateStatement<Status>() {
					@Override
					public Status run(Session session) {
						Long statusId = (Long) session.createCriteria(Status.class).add
							(Restrictions.eq("user.id", id)).
							setProjection(Projections.max("id")).uniqueResult();
						session.flush();
						return Status.createStatus(statusId, User.createUser(-1L, "name", 0), "", new DateTime(2010, 1, 1, 12, 0, 0, 0), false);
					}
				});
	}
	
	/**
	 * Gets a batch of unprocessed users
	 * 
	 * @param count
	 * 			- The max number of users to pull
	 * @return A list of statuses
	 */
	public List<Status> getUnprocessed(final int count) {
		return HibernateUtil
		.doWithSession(new HibernateStatement<List<Status>>() {
			@SuppressWarnings("unchecked")
			@Override
			public List<Status> run(Session session) {
				List<Status> status = session.createCriteria(Status.class).add(
						Restrictions.eq("processed", false)).setMaxResults(count).list();
				session.flush();
				return status;
			}
		});
	}
	
	/**
	 * Gets the 5 oldest statuses for the specified hash
	 * 
	 * @param key
	 * 			- The hash value
	 * @return A list of the top starters
	 */
	public List<Status> getStartersForHash(final String key, final int count) {
		return HibernateUtil.doWithSession(new HibernateStatement<List<Status>>() {
			@SuppressWarnings("unchecked")
			@Override
			public List<Status> run(Session session) {
				List<Status> status = (List<Status>) session.createQuery("select s from Hash h join h.statuses s where h.hash = ? order by s.statusDate desc")
					.setString(0, key)
					.setMaxResults(count).list();
				session.flush();
				return status;
			}
		});
	}
	
	/**
	 * Gets the total statuses in the table
	 * 
	 * @return A long with the total number of rows
	 */
	public Long getCount() {
		return HibernateUtil.doWithSession(new HibernateStatement<Long>() {
			@Override
			public Long run(Session session) {
				Long count = (Long) session.createQuery("select count(*) from Status").uniqueResult();
				session.flush();
				return count;
			}
		});
	}
}
