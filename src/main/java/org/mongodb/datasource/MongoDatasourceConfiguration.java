package org.mongodb.datasource;

import com.mongodb.MongoClientOptions;
import javax.naming.RefAddr;
import javax.naming.Reference;

public class MongoDatasourceConfiguration {
	
	// Server constants
	private static final String PROP_HOST = "host";
	private static final String PROP_PORT = "port";
	// Authentication constants
	private static final String PROP_DB_NAME	= "databaseName";
	private static final String PROP_USERNAME	= "username";
	private static final String PROP_PASSWORD	= "password";
	private static final String PROP_AUTH_MECHANISM = "authMechanism";
	
	// Pooling constants
	private static final String PROP_MIN_POOL_SIZE	= "minPoolSize";
	private static final String PROP_MAX_POOL_SIZE	= "maxPoolSize";
	private static final String PROP_MAX_WAIT_TIME	= "maxWaitTime";

	// Server settings
	private String host	= "localhost";
	private int port	= 27017;
	// Authentication settings
	private String databaseName	= null;
	private String username		= null;
	private String password		= null;
	private String authMechanism= null;
	// Pooling settings
	private Integer minPoolSize	= null;
	private Integer maxPoolSize	= null;
	private Integer maxWaitTime	= null;

	
	public static MongoDatasourceConfiguration loadFromJNDIReference(final Reference reference) throws Exception {
		final MongoDatasourceConfiguration config = new MongoDatasourceConfiguration();

		// Server settings
		
		// Optional DB host
		String host = getReferenceValue(reference, PROP_HOST);
		if (!isEmptyValue(host)) {
			config.host = host;
		}
		// Optional DB port
		Integer optionalIntValue = getOptionalReferenceValueAsInteger(reference, PROP_PORT);
		if (optionalIntValue != null) {
			config.port = optionalIntValue.intValue();
		}
		
		// Authentication settings
		
		// DB name
		config.databaseName = getReferenceValue(reference, PROP_DB_NAME);
		if (isEmptyValue(config.databaseName)) {
			throw new Exception("Missing value for mandatory property "+ PROP_DB_NAME);
		}
		// Optional user name
		config.username = getReferenceValue(reference, PROP_USERNAME);
		// Optional password (mandatory if user name is specified)
		config.password = getReferenceValue(reference, PROP_PASSWORD);
		if ((!isEmptyValue(config.username)) && (isEmptyValue(config.password)))
			throw new Exception("Missing password property: "+ PROP_PASSWORD);
		
		// Optional Authentication Mechanism
		config.authMechanism = getReferenceValue(reference, PROP_AUTH_MECHANISM);
		if(isEmptyValue(config.authMechanism) && !isEmptyValue(config.username) && !isEmptyValue(config.password)){			
			config.authMechanism = "SCRAM-SHA-1";
		}
		
		if(!isEmptyValue(config.authMechanism) && (isEmptyValue(config.username) || isEmptyValue(config.password)) ){
			throw new Exception("Missing value for mandatory property "+ PROP_USERNAME + " or " + PROP_PASSWORD);
		}
		// Pooling settings
		
		// Optional minimum pool size
		optionalIntValue = getOptionalReferenceValueAsInteger(reference, PROP_MIN_POOL_SIZE);
		if (optionalIntValue != null)
			config.minPoolSize = optionalIntValue;
		// Optional maximum pool size
		optionalIntValue = getOptionalReferenceValueAsInteger(reference, PROP_MAX_POOL_SIZE);
		if (optionalIntValue != null) 
			config.maxPoolSize = optionalIntValue;
		// Optional maximum wait time to retrieved a connection from the pool
		optionalIntValue = getOptionalReferenceValueAsInteger(reference, PROP_MAX_WAIT_TIME);
		if (optionalIntValue != null)
			config.maxWaitTime = optionalIntValue;
		
		return config;
	}

	/**
	 * Assembles a MongoClientOptions object based on datasource configuraton
	 * @param datasourceName name of the datasource
	 * @return MongoClientOptions initialized with datasource configuration
	 */
	public MongoClientOptions getMongoClientOptions(final String datasourceName) {
		final MongoClientOptions.Builder optionBuilder = MongoClientOptions.builder();

		// Set DS name
		optionBuilder.description(datasourceName);
		
		// Set min pool size
		if (this.minPoolSize != null)
			optionBuilder.minConnectionsPerHost(this.minPoolSize.intValue());
		// Set max pool size
		if (this.maxPoolSize != null)
			optionBuilder.connectionsPerHost(this.maxPoolSize.intValue());
		// Set max wait time
		if (this.maxWaitTime != null)
			optionBuilder.maxWaitTime(this.maxWaitTime.intValue());
		
		return optionBuilder.build();
	}

	private static Integer getOptionalReferenceValueAsInteger(final Reference reference, final String key) throws Exception {
		String stringValue = getReferenceValue(reference, key);
		if (!isEmptyValue(stringValue)) {
			try {
				return Integer.valueOf(Integer.parseInt(stringValue));
			} catch (NumberFormatException e) {
				throw new Exception("Invalid " + key + " value " + stringValue, e);
			}
		}
		return null;
	}

	private static String getReferenceValue(final Reference reference, final String key) {
		RefAddr property = reference.get(key);
		return property == null ? null : (String) property.getContent();
	}

	private static boolean isEmptyValue(final String value) {
		return (value == null) || (value.trim().isEmpty());
	}

	public String toString() {
		final StringBuffer output = new StringBuffer("MongoDatasourceConfiguration {");
		// Server settings
		output.append(PROP_HOST).append("=").append(this.host).append(", ");
		output.append(PROP_PORT).append("=").append(this.port).append(", ");
		// Authentication settings
		output.append(PROP_DB_NAME).append("=").append(this.databaseName).append(", ");
		output.append(PROP_USERNAME).append("=").append(this.username).append(", ");
		output.append(PROP_AUTH_MECHANISM).append("=").append(this.authMechanism).append(",");
		// Pooling settings
		output.append(PROP_MIN_POOL_SIZE).append("=").append(this.minPoolSize).append(", ");
		output.append(PROP_MAX_POOL_SIZE).append("=").append(this.maxPoolSize).append(", ");
		output.append(PROP_MAX_WAIT_TIME).append("=").append(this.maxWaitTime);
		output.append("}");
		return output.toString();
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public String getDatabaseName() {
		return this.databaseName;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public Integer getMinPoolSize() {
		return this.minPoolSize;
	}

	public Integer getMaxPoolSize() {
		return this.maxPoolSize;
	}

	public Integer getMaxWaitTime() {
		return this.maxWaitTime;
	}
	
	public String getAuthMechanism() {
		return authMechanism;
	}
}
