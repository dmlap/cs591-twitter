package edu.bu.entities;

import org.hibernate.classic.Session;
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
	public void save(final User target) {
		try {
			HibernateUtil.doWithSession(new HibernateStatement<Void>() {
			@Override
			public Void run(Session session) {
				session.save(target);		
				return null;
			}
		});
		} catch (Exception ex) {
			System.out.println("Error saving");
		}
	}
	
}
