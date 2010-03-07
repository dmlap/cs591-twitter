package edu.bu.entities;

import org.hibernate.*;

public class TestHarness {
	public static void main(String[] args) throws Exception {
		Session session = HibernateUtil.getSession();
		Query qry = session.createQuery("SELECT COUNT(*) FROM Users");
		System.out.println(qry.toString());
	}
}
