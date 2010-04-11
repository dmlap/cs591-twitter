package edu.bu.entities;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.classic.Session;

public class HibernateUtil {

	private static final SessionFactory sessionFactory = new AnnotationConfiguration()
			.addPackage("edu.bu.entities")
			.addAnnotatedClass(Users.class)
			.addAnnotatedClass(Statuses.class)
			// db properties
			.setProperty("hibernate.connection.driver_class", "org.h2.Driver")
			.setProperty("hibernate.connection.url", "jdbc:h2:~/cs591-twitter;AUTO_SERVER=TRUE")
			.setProperty("hibernate.connection.username", "sa").setProperty(
					"hibernate.connection.password", "").setProperty("dialect",
					"org.hibernate.dialect.HSQLDialect").setProperty(
					"hibernate.hbm2ddl.auto", "update").setProperty(
					"hibernate.connection.provider_class",
					"org.hibernate.connection.C3P0ConnectionProvider")
			.setProperty("hibernate.c3p0.min_size", "5").setProperty(
					"hibernate.c3p0.max_size", "20").setProperty(
					"hibernate.current_session_context_class",
					"org.hibernate.context.ThreadLocalSessionContext")
			.configure().buildSessionFactory();
	private static final BeforeTransaction defaultBeforeTransaction = new BeforeTransaction() {
		@Override
		public void before(Transaction transaction) {
		}
	};
	public static BeforeTransaction beforeTransaction = defaultBeforeTransaction;
	public static AfterTransaction defaultAfterTransaction = new AfterTransaction() {
		@Override
		public void after(Transaction transaction) {
			transaction.commit();
		}
	};
	public static AfterTransaction afterTransaction = defaultAfterTransaction;
	public static OnError defaultOnError = new OnError() {
		@Override
		public void handleError(Throwable cause, Session session,
				Transaction transaction) {
			cause.printStackTrace();
			transaction.rollback();
		}
	};
	public static OnError onError = defaultOnError;
	public static void reset() {
		beforeTransaction = defaultBeforeTransaction;
		afterTransaction = defaultAfterTransaction;
		onError = defaultOnError;
	}

	public static <T> T doWithSession(HibernateStatement<T> statement) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {	
			beforeTransaction.before(transaction);
			T result = statement.run(session);
			afterTransaction.after(transaction);
			return result;
		} catch (Throwable throwable) {
			onError.handleError(throwable, session, transaction);
			throw new RuntimeException(
					"Exception thrown during Hibernate statement, rolling back transaction",
					throwable);
		} finally {
			session.close();
		}
	}

	public static interface HibernateStatement<T> {
		public T run(Session session);
	}

	public static interface BeforeTransaction {
		void before(Transaction transaction);
	}

	public static interface AfterTransaction {
		void after(Transaction transaction);
	}

	public static interface OnError {
		void handleError(Throwable cause, Session session,
				Transaction transaction);
	}
}
