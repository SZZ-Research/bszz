package nz.ac.otago.persistence.factory;

import nz.ac.otago.persistence.LinkedIssueDAO;
import nz.ac.otago.enums.DAOType;

public abstract class FactoryDAO {

	public abstract LinkedIssueDAO getLinkedIssueDAO();

	public static FactoryDAO getFactoryDAO(DAOType type) {
		if(type == DAOType.HIBERNATE)
		{
			return new HibernateFactoryDAO();
		}
		else
		{
			return null;
		}
	}
	
}
