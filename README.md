# MongoDB Queryable Encryption Explicit Range example

An example of explicit queryable encryption with MongoDB 8.0

Get started quickly using MongoDB and Java.

Using the gradle wrapper to manage dependencies all that's needed to get up and running and connecting to MongoDB via 
the Java Driver is:

./gradlew run

The `ExplicitEncryption` class will connect to MongoDB, create a collection with encrypted options. 
Add some encrypted test data,
then use `encryptExpression` to create a range query to run against the collection.

Finally, there is an example of mixing the encrypted range query, with additional field filtering.


