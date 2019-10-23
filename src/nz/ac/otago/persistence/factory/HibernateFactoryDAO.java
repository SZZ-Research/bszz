package nz.ac.otago.persistence.factory;

import nz.ac.otago.persistence.LinkedIssueDAO;
import nz.ac.otago.persistence.LinkedIssueDAOImpl;

public class HibernateFactoryDAO extends FactoryDAO {

	/* (non-Javadoc)
	 * @see br.ufrn.backhoe.persistence.factory.FactoryDAO#getHibernateLinkedIssueSvnDAO()Li
	 */
	@Override
	public LinkedIssueDAO getLinkedIssueDAO() {
		return new LinkedIssueDAOImpl();
	}
}

