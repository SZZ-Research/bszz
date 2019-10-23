package nz.ac.otago.connector.factory;

import nz.ac.otago.connector.Connector;

public abstract class ConnectorFactory {

	public abstract Connector createConnector(String user, String password, String path, String url) throws Exception;
}
