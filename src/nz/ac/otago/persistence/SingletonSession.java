package nz.ac.otago.persistence;

import org.hibernate.Session;
import nz.ac.otago.utils.HibernateUtil;

public class SingletonSession {
	
	private static Session hibernateSession; 
	
	public static Session getSession(String configName) {
		if(hibernateSession != null) {
			return hibernateSession;
		}
		else {
			hibernateSession = HibernateUtil.getMySessionFactory(configName).openSession();
			return hibernateSession;
		}
	}
}
