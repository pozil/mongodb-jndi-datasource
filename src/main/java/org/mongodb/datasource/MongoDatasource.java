package org.mongodb.datasource;

import com.mongodb.DB;
import com.mongodb.MongoClient;

/**
 * Bean holding an open MongoClient used as a pooled datasource and its configuration
 * @author Philippe Ozil
 */
public class MongoDatasource {
	private final MongoClient client;
	private final MongoDatasourceConfiguration config;

	protected MongoDatasource(MongoClient client, MongoDatasourceConfiguration config) {
		this.client = client;
		this.config = config;
	}

	/**
	 * Retrieves a connection from this datasource
	 * @return DB object representing a MongoDB connection
	 */
	public DB getConnection() {
		return client.getDB(config.getDatabaseName());
	}

	/**
	 * Closes all datasource connections
	 */
	public void close() {
		client.close();
	}
}
