package edu.bu.entities;

import static org.junit.Assert.assertEquals;

import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.bu.entities.HibernateUtil.AfterTransaction;

public class StatusesDaoTest {
	
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
		StatusesDao dao = new StatusesDao();
		Statuses status = Statuses.createStatus(1L, 1L, "status", new DateTime(2010, 1, 1, 12, 0, 0, 0), false);
		dao.save(status);
		Statuses status0 = dao.get(status.getId());
		
		assertEquals(status, status0);
	}
}
