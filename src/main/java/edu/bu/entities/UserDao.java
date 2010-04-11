package edu.bu.entities;

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
	public void save(final Users target) {
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
