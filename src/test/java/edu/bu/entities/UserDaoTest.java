package edu.bu.entities;

import static org.junit.Assert.assertEquals;

import org.hibernate.Transaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.bu.entities.HibernateUtil.AfterTransaction;

public class UserDaoTest {
	
	@BeforeClass
	public static void setupTransactions() {
		HibernateUtil.afterTransaction = new AfterTransaction() {
			@Override
			public void after(Transaction transaction) {
				transaction.rollback();
			}
		};
	}
	
	@AfterClass
	public static void resetTransactions() {
		HibernateUtil.reset();
	}
	
	@Test
	public void saveLoad() {
		UserDao dao = new UserDao();
		Users user = Users.createUser(1L, "username", 0);
		dao.save(user);
		Users user0 = dao.get(user.getId());
		
		assertEquals(user, user0);
	}

}
