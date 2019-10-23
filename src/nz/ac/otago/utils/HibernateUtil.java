package nz.ac.otago.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class HibernateUtil {
	private static ServiceRegistry serviceRegistry;
	
	private static SessionFactory buildMySessionFactory(String configName) {
		try {
			Configuration config = new Configuration();
			config.configure(configName);
			
			serviceRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();
			return config.buildSessionFactory(serviceRegistry);
		}
		catch(Throwable ex) {
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}
	
	public static SessionFactory getMySessionFactory(String config) {
		return buildMySessionFactory(config);
	}
}
