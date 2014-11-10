mongodb-jndi-datasource
=======================
**A JNDI pooled datasource for MongoDB**

I have tested it on Tomcat 6 but it should work fine on other Java servers.


##Deploy instructions for Tomcat 6:
In these instructions, I explain how to bind the datasource (DS) to the Bonita web application.

1. Obtain the Mongo DB client JAR file from here 
2. Obtain this project's JAR file
3. Place the 2 aforementioned JAR files in this directory: TOMCAT_HOME/lib/
4. Edit this file (for binding the DS to Bonita): TOMCAT_HOME/conf/Catalina/localhost/bonita.xml
5. Add this block of XML somewhere in the "Context" tag

``` java
<Resource name="testMongodbDS"
	auth="Container"
	type="com.mongodb.DB"
	factory="org.mongodb.datasource.MongoDatasourceFactory"
	
	host="localhost"
	port="27017"
	
	databaseName="test"
	username=""
	password=""
	
	minPoolSize="10"
	maxPoolSize="100"
	maxWaitTime="10000"
/>
```

6. Update these settings to match your MongoDB settings


##Usage instructions:
Once you have deployed the datasource on your server, you may call it with the following Java code:

``` java
Context initCtx = new InitialContext();
DB db = (DB) initCtx.lookup("java:/comp/env/testMongodbDS");

// Fetch connection from pool
db.requestStart();
try
{
	// Make sure connection is valid
	db.requestEnsureConnection();
	// Perform a query
	LOGGER.severe("Mongo collections: "+ db.getCollectionNames());
}
finally
{
	// Release connection to pool
	db.requestDone();
}
```

Note that the returned DB object is the one from the official MongoDB driver:
http://docs.mongodb.org/ecosystem/tutorial/getting-started-with-java-driver/

