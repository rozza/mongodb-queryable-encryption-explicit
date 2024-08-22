# MongoDB Queryable Encryption Explicit Range example

An example of explicit queryable encryption with MongoDB 8.0

Get started quickly using MongoDB and Java.

Using the gradle wrapper to manage dependencies all that's needed to get up and running and connecting to MongoDB via 
the Java Driver is:

```
./gradlew run
```

To run the multiple encrypted field example:

```
./gradlew run -Dexample=multi
```

The `ExplicitEncryptionSingle` or `ExplicitEncryptionMulti` class will:  connect to MongoDB, 

- create a collection with encrypted options. 
- Insert some encrypted test data,
- Use `encryptExpression` to create a range query to run against the collection.
- Run an example of mixing the encrypted range query, with additional field filtering.

