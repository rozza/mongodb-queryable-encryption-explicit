/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package demo;

import static java.util.Collections.singletonList;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.client.model.vault.RangeOptions;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.conversions.Bson;

public class ExplicitEncryption {

    private static final String ENCRYPTED_FIELD = "encryptedInt";

    public static void main(String[] args) {
        System.out.println("============================");
        System.out.println("         STARTING");
        System.out.println("============================");

        String mongoConnectionString = args.length == 0 ? "mongodb://localhost:27017" : args[0];

        // This would have to be the same master key as was used to create the encryption key
        byte[] localMasterKey = new byte[96];
        new SecureRandom().nextBytes(localMasterKey);

        Map<String, Map<String, Object>> kmsProviders = new HashMap<>() {
            {
                put("local", new HashMap<>() {
                    {
                        put("key", localMasterKey);
                    }
                });
            }
        };

        String keyVaultNamespace = "encryption.__keyVault";
        ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
                .keyVaultMongoClientSettings(MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(mongoConnectionString))
                        .build())
                .keyVaultNamespace(keyVaultNamespace)
                .kmsProviders(kmsProviders)
                .build();

        try (ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings)) {
            BsonBinary dataKeyId = clientEncryption.createDataKey("local", new DataKeyOptions());

            RangeOptions rangeOptions = new RangeOptions()
                    .min(new BsonInt32(0))
                    .max(new BsonInt32(200))
                    .trimFactor(1)
                    .sparsity(1L);

            EncryptOptions encryptValueOptions = new EncryptOptions("Range")
                    .keyId(dataKeyId)
                    .contentionFactor(0L)
                    .rangeOptions(rangeOptions);

            EncryptOptions encryptQueryOptions = new EncryptOptions("Range")
                    .keyId(dataKeyId)
                    .queryType("Range")
                    .contentionFactor(0L)
                    .rangeOptions(rangeOptions);

            MongoClientSettings clientSettings = MongoClientSettings.builder()
                    .autoEncryptionSettings(AutoEncryptionSettings.builder()
                            .keyVaultNamespace(keyVaultNamespace)
                            .kmsProviders(kmsProviders)
                            .bypassQueryAnalysis(true)
                            .build()) // Auto encryption settings are required
                    .build();

            try (MongoClient mongoClient = MongoClients.create(clientSettings)) {
                String databaseName = "testExplicitEncryption";
                String collectionName = "explicit_encryption";

                MongoDatabase database = mongoClient.getDatabase(databaseName);
                database.drop();
                MongoCollection<BsonDocument> collection = database.getCollection(collectionName, BsonDocument.class);

                BsonDocument encryptedFields = new BsonDocument()
                        .append(
                                "fields",
                                new BsonArray(singletonList(new BsonDocument("keyId", dataKeyId)
                                        .append("path", new BsonString(ENCRYPTED_FIELD))
                                        .append("bsonType", new BsonString("int"))
                                        .append(
                                                "queries",
                                                new BsonDocument("queryType", new BsonString("range"))
                                                        .append("contention", new BsonInt64(0L))
                                                        .append("trimFactor", new BsonInt32(1))
                                                        .append("sparsity", new BsonInt64(1))
                                                        .append("min", new BsonInt32(0))
                                                        .append("max", new BsonInt32(200))))));

                database.createCollection(
                        collectionName, new CreateCollectionOptions().encryptedFields(encryptedFields));

                // Explicitly encrypt a field
                System.out.println("\n  > Inserting some data");
                List<BsonDocument> documents = IntStream.range(1, 10)
                        .boxed()
                        .map(i -> new BsonDocument("_id", new BsonInt32(i))
                                .append(
                                        ENCRYPTED_FIELD,
                                        encryptValue(clientEncryption, new BsonInt32(i), encryptValueOptions)))
                        .toList();
                InsertManyResult insertManyResult = collection.insertMany(documents);
                System.out.println(
                        "    Inserted: " + insertManyResult.getInsertedIds().size() + " documents.");

                System.out.println("\n  > Example of the data without decryption:\n");
                System.out.println(collection.find().into(new ArrayList<>()).stream()
                        .map(BsonDocument::toJson)
                        .collect(Collectors.joining(",\n    ", "    ", "\n")));

                System.out.println("\n  > Finding the data within a range using encryptExpression:\n");
                Bson encryptExpression = Filters.and(Filters.gte(ENCRYPTED_FIELD, 5));

                System.out.println(
                        "  Query: " + encryptExpression.toBsonDocument().toJson());
                BsonDocument encryptedQuery =
                        encryptExpression(clientEncryption, encryptExpression, encryptQueryOptions);

                System.out.println(
                        collection.find(encryptedQuery).sort(Sorts.ascending("_id")).into(new ArrayList<>()).stream()
                                .map(BsonDocument::toJson)
                                .collect(Collectors.joining(",\n    ", "    ", "\n")));

                System.out.println("  Mixing encrypted range query with other criteria.");
                ArrayList<BsonValue> andQuery = new ArrayList<>();
                andQuery.add(new BsonDocument("_id", new BsonInt32(6)));
                andQuery.addAll(encryptedQuery.getArray("$and").getValues());
                BsonDocument mixedQuery = new BsonDocument("$and", new BsonArray(andQuery));
                System.out.println("  Query Mixed: " + mixedQuery + " \n");

                System.out.println(
                        collection.find(mixedQuery).sort(Sorts.ascending("_id")).into(new ArrayList<>()).stream()
                                .map(BsonDocument::toJson)
                                .collect(Collectors.joining(",\n    ", "    ", "\n")));
            }
        }

        System.out.println("============================");
        System.out.println("         fin.");
        System.out.println("============================");
        System.out.println("\n");
    }

    private static BsonValue encryptValue(
            ClientEncryption clientEncryption, BsonValue value, EncryptOptions encryptOptions) {
        return clientEncryption.encrypt(value, encryptOptions);
    }

    private static BsonDocument encryptExpression(
            ClientEncryption clientEncryption, Bson expression, EncryptOptions encryptOptions) {
        return clientEncryption.encryptExpression(expression, encryptOptions);
    }
}
