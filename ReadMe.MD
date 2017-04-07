# techlab-fuse-jdg-simple-hotrod 
=========================================
This project shows how to setup a remote connection to Data Grid in Fuse via Hot Rod Client. 

![overview](https://raw.githubusercontent.com/alainpham/alainpham.github.io/master/assets/images/posts/fuse-data-grid-integration-via-hot-rod-with-persistence-and-indexing-for-advanced-queries/diag.png)

Blog post explaining this project can be found here : 

https://alainpham.github.io/posts/fuse-data-grid-integration-via-hot-rod-with-persistence-and-indexing-for-advanced-queries/


To build this project use
```
    mvn install
```
To run the project you can execute the following Maven goal
```
    mvn clean package camel:run
```
For more help see the Apache Camel documentation
```
    http://camel.apache.org/
```

# Run a H2 data base that will act as cache store for your JBoss Data Grid

https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/h2database/h2-2012-07-13.zip
```
java -cp h2*.jar org.h2.tools.Server -tcp -tcpAllowOthers -tcpPort 8942 -baseDir ./h2dbstore -web -webAllowOthers -webPort 11112
```
# Configure your JBoss Data Grid server as follows (i.e in the standalone.xml ) and run bin/standalone.sh
```
<subsystem xmlns="urn:jboss:domain:datasources:1.2">
	<datasources>
		<datasource jndi-name="java:jboss/datasources/JdbcDS" pool-name="JdbcDS" enabled="true" use-java-context="true">
		    <connection-url>jdbc:h2:tcp://localhost:8942/dgdb</connection-url>
		    <driver>h2</driver>
		    <security>
		        <user-name>sa</user-name>
		        <password></password>
		    </security>
		</datasource>
		<drivers>
		    <driver name="h2" module="com.h2database.h2">
		        <xa-datasource-class>org.h2.jdbcx.JdbcDataSource</xa-datasource-class>
		    </driver>
		</drivers>
	</datasources>
</subsystem>
<subsystem xmlns="urn:infinispan:server:core:6.3" default-cache-container="local">
	<local-cache name="event" >
		<eviction strategy="LRU" max-entries="50"  /> 		
		<indexing  index="LOCAL" >
			<property name="default.directory_provider">filesystem</property>
			<property name="default.indexBase">ispn_index</property>
		</indexing>
	    <mixed-keyed-jdbc-store datasource="java:jboss/datasources/JdbcDS" passivation="false" preload="true" purge="false">
	        <!-- property name="databaseType">H2</property -->
	        <binary-keyed-table prefix="ISPN_MIX_BKT" create-on-start="true" drop-on-exit="false">
	            <id-column name="id" type="VARCHAR"/>
	            <data-column name="datum" type="BINARY"/>
	            <timestamp-column name="version" type="BIGINT"/>
	        </binary-keyed-table>
	        <string-keyed-table prefix="ISPN_MIX_STR" create-on-start="true" drop-on-exit="false">
	            <id-column name="id" type="VARCHAR"/>
	            <data-column name="datum" type="BINARY"/>
	            <timestamp-column name="version" type="BIGINT"/>
	        </string-keyed-table>
	    </mixed-keyed-jdbc-store>
	</local-cache>
</subsystem>
```
# Run Fuse project and test

```
mvn clean package camel:run
```

Put some data

```
curl -X PUT --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
"uid": "1",
"timestmp": "2017-04-07T19:30:00.000Z",
"name": "start",
"content": "party started" }' 'http://localhost:7123/event/1'

curl -X PUT --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
"uid": "2",
"timestmp": "2017-04-07T22:15:00.000Z",
"name": "incident",
"content": "police arrived" }' 'http://localhost:7123/event/2'

curl -X PUT --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
"uid": "3",
"timestmp": "2017-04-07T23:18:00.000Z",
"name": "incident",
"content": "host arrested" }' 'http://localhost:7123/event/3'

curl -X PUT --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
"uid": "4",
"timestmp": "2017-04-07T23:20:00.000Z",
"name": "end",
"content": "party ended" }' 'http://localhost:7123/event/4'
 ```


List all events through a query without parameters

```
curl -X GET --header 'Accept: application/json' 'http://localhost:7123/query/event/techlab.model.Event
```

List all incidents through a query with a parameter
```
curl -X GET --header 'Accept: application/json' 'http://localhost:7123/query/event/techlab.model.Event?name=incident'
```
List all incidents at a certain time with 2 parameters
```
curl -X GET --header 'Accept: application/json' 'http://localhost:7123/query/event/techlab.model.Event?name=incident&timestmp=1491607080000'
```
