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

The examples are as follows:

- "single" - a single range field
- "multi" - multiple range fields
- "multiCollection" - shared MongoClient with multiple different encryption types in the multiple collections
- "multiMixed" - multiple different encryption types in the same collection - expected to fail.
- "query" - mapping queries

The examples will:

- create a collection with encrypted options.
- Insert some encrypted test data,
- Encrypt values to create a range query to run against the collection.
- Run an example of mixing the encrypted range query, with additional field filtering.
