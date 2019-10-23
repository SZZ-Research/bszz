package nz.ac.otago.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.*;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.apache.log4j.Logger;

public abstract class AbstractDAO<T> {
	protected Connection connection;
	protected Session currentSession;
	protected final Logger log = Logger.getLogger(AbstractDAO.class);

	public AbstractDAO() {
		currentSession = SingletonSession.getSession("/hibernate.cfg.xml");
	}

	public void prepareBatchInsert(){
	}

	public void addBatch(T obj){
	}

	public int[] executeBatch(){
		return null;
	}

	public T load(Long id){
		return null;
	}

	public void persist(T obj){
		currentSession.persist(obj);
	}

	public void update(T obj){
		currentSession.saveOrUpdate(obj);
	}

	public void remove(T obj){
		currentSession.delete(obj);
	}

	public void executeSQL(String sql) {
		SQLQuery query = currentSession.createSQLQuery(sql);
		query.executeUpdate();
	}

	public void executeSQLWithParams(String sql, Object... values){
		SQLQuery query = currentSession.createSQLQuery(sql);

		int count = 1;
		for(Object value : values){
			String paramName = "param"+count;
			query.setParameter(paramName,value);
			count++;
		}

		query.executeUpdate();
	}

	public List<Object[]> executeSQLQuery(String sql) {
		SQLQuery query = currentSession.createSQLQuery(sql);
		List<Object[]> objs = query.list();
		return objs;
	}

	public List<Object[]> executeSQLQueryWithParams(String sql, Object... values) {
		SQLQuery query = currentSession.createSQLQuery(sql);
		int count = 1;
		for(Object value : values){
			String paramName = "param"+count;
			query.setParameter(paramName,value);
			count++;
		}

		List<Object[]> objs = query.list();
		return objs;
	}

	public Transaction beginTransaction() {
		Transaction tx = currentSession.beginTransaction();
		return tx;
	}

	public void commit(Transaction transaction) {
		transaction.commit();
		currentSession.clear();
	}

	public void disconnect() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
