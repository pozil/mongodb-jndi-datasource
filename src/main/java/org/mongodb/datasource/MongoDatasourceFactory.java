package org.mongodb.datasource;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * A MongoDB JDNI pooled datasource that can be used in a Java server
 * @author Philippe Ozil
 */
public class MongoDatasourceFactory implements ObjectFactory {
	private static final Logger LOGGER = Logger.getLogger(MongoDatasourceFactory.class.getName());
	private static final Map<String, MongoDatasource> datasources = new ConcurrentHashMap<String, MongoDatasource>();

	public Object getObjectInstance(Object referenceObject, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("[DS " + name + "] Called");
		}
		try {
			// Fetch datasource and return a connection
			final MongoDatasource datasource = getDatasource(referenceObject, name.toString());
			return datasource.getConnection();
		} catch (Exception e) {
			throw new Exception("[DS " + name + "] Error: " + e.getMessage(), e);
		}
	}

	/**
	 * Fetches a datasource from its name OR load it then cache it if is a first-time use 
	 * @param referenceObject JNDI reference object
	 * @param dsName datasource name
	 * @return  MongoDatasource
	 * @throws Exception
	 */
	private static synchronized MongoDatasource getDatasource(final Object referenceObject, final String dsName) throws Exception {
		MongoDatasource datasource = (MongoDatasource) datasources.get(dsName);
		// Check if datasource is already initialized
		if (datasource == null)
		{
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("[DS " + dsName + "] Initializing datasource for first use...");
			}
			// Check datasource reference
			if ((referenceObject == null) || (!(referenceObject instanceof Reference)))
				throw new Exception("[DS " + dsName + "] Invalid JNDI object reference");
			final Reference reference = (Reference) referenceObject;

			// Load datasource
			try {
				datasource = loadDatasource(dsName, reference);
			} catch (Exception e) {
				throw new Exception("Failed to load configuration: " + e.getMessage(), e);
			}
			
			// Add datasource to cache
			datasources.put(dsName, datasource);
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("[DS " + dsName + "] Datasource initialized and added to cache");
			}
		} else if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("[DS " + dsName + "] Using cached datasource");
		}

		return datasource;
	}

	/**
	 * Loads a datasource from the provided JDNI reference
	 * @param dsName datasource name
	 * @param reference JDNI reference containing datasource settings
	 * @return MongoDatasource
	 * @throws Exception
	 */
	private static MongoDatasource loadDatasource(final String dsName, final Reference reference) throws Exception {
		// Load datasource configuration
		final MongoDatasourceConfiguration config = MongoDatasourceConfiguration.loadFromJNDIReference(reference);
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("[DS " + dsName + "] Configuration loaded: " + config);
		}

		// Prepare Mongo client configuration
		final MongoClientOptions options = config.getMongoClientOptions(dsName);
		final ServerAddress serverAddress = new ServerAddress(config.getHost(), config.getPort());

		// Create Mongo client instance
		final MongoClient mongoClient;
		if (config.getUsername() != null && !config.getUsername().isEmpty()) {
			// Perform authenticated connection
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("[DS " + dsName + "] Attempting to create authenticated connection...");
			final MongoCredential credential = MongoCredential.createMongoCRCredential(config.getUsername(), config.getDatabaseName(), config.getPassword().toCharArray());
			mongoClient = new MongoClient(serverAddress, Arrays.asList(new MongoCredential[] { credential }), options);
		}
		else
		{
			// Perform anonymous connection
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("[DS " + dsName + "] Attempting to create anonymous connection...");
			mongoClient = new MongoClient(serverAddress, options);
		}

		return new MongoDatasource(mongoClient, config);
	}
}
