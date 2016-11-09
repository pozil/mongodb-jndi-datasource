package org.mongodb.datasource;

public enum MongoAuthenticationMechanism {
	SCRAM_SHA_1("SCRAM-SHA-1"),
	MONGODB_CR("MONGODB-CR");
	
	private String value;
	
	private MongoAuthenticationMechanism(String value) {
		this.value = value;
	}

	public String toString() {
		return value;
	}
	
	public static MongoAuthenticationMechanism getFromValue(String valueString) {
		if (SCRAM_SHA_1.value.equalsIgnoreCase(valueString))
			return MongoAuthenticationMechanism.SCRAM_SHA_1;
		else if (MONGODB_CR.value.equalsIgnoreCase(valueString))
			return MongoAuthenticationMechanism.MONGODB_CR;
		else
			throw new IllegalArgumentException("Invalid Mongo authentication mechanism: "+ valueString);
	}
}
