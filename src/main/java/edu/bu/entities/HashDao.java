package edu.bu.entities;

import java.util.List;

import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

import edu.bu.entities.HibernateUtil.HibernateStatement;

public class HashDao implements Dao<Hash, String>{
	@Override
	public Hash get(final String key) {
		return HibernateUtil.doWithSession(new HibernateStatement<Hash>() {
			@Override
			public Hash run(Session session) {
				Hash hash = (Hash) session.createCriteria(Hash.class).add(
						Restrictions.idEq(key)).uniqueResult();
				session.flush();
				return hash;
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
				session.flush();
				return null;
			}
		});
	}
	
	@Override
	public void update(final Hash target, final Hash... targets) {
		HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.evict(target);
				session.update(target);
				for(Hash t : targets) {
					session.evict(t);
					session.update(t);
				}
				session.flush();
				return null;
			}
		});
	}
	
	/**
	 * Gets a set of hashes where processed = false
	 * 
	 * @param count
	 * 			- The max number of hashes to get
	 * @return A list of hashes
	 */
	public List<Hash> getUnprocessed(final int count) {
		return HibernateUtil.doWithSession(new HibernateStatement<List<Hash>>() {
			@SuppressWarnings("unchecked")
			@Override
			public List<Hash> run(Session session) {
				List<Hash> hash = (List<Hash>) session.createQuery("select h from Hash h where h.processed = false")
					.setMaxResults(count).list();
				session.flush();
				return hash;
			}
		});
	}
	
	/**
	 * Gets a set of hashes where processed = true
	 * 
	 * @param count
	 * 			- The max number of hashes to get
	 * @return A list of hashes
	 */
	public List<Hash> getProcessed(final int count) {
		return HibernateUtil.doWithSession(new HibernateStatement<List<Hash>>() {
			@SuppressWarnings("unchecked")
			@Override
			public List<Hash> run(Session session) {
				List<Hash> hash = (List<Hash>) session.createQuery("select h from Hash h where h.processed = true")
					.setMaxResults(count).list();
				session.flush();
				return hash;
			}
		});
	}
	
	/**
	 * Gets the total hashes in the table
	 * 
	 * @return A long with the total number of rows
	 */
	public Long getCount() {
		return HibernateUtil.doWithSession(new HibernateStatement<Long>() {
			@Override
			public Long run(Session session) {
				Long count = (Long) session.createQuery("select count(*) from Hash").uniqueResult();
				session.flush();
				return count;
			}
		});
	}
	
	/**
	 * Gets the top ten users by degree
	 * 
	 * @return List of the 10 users
	 */
	public List<Hash> topTen() {
		return HibernateUtil
				.doWithSession(new HibernateStatement<List<Hash>>() {
					@SuppressWarnings("unchecked")
					@Override
					public List<Hash> run(Session session) {
						List<Hash> hash = (List<Hash>) session.createQuery("select h from Hash h join h.statuses s group by h order by count(s) desc")
							.setMaxResults(10).list();
						session.flush();
						return hash;
					}
				});
	}
}
