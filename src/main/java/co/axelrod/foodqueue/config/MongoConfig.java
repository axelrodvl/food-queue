package co.axelrod.foodqueue.config;

import co.axelrod.foodqueue.model.Queue;
import co.axelrod.foodqueue.model.User;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
public class MongoConfig {
    private static final String MODEL_PACKAGE = "co.axelrod.foodqueue.model";

    @Value("${mongo.host}")
    private String host;

    @Value("${mongo.port}")
    private Integer port;

    @Value("${mongo.database}")
    private String database;

    @Bean
    public MongoDatabase mongoDatabase() {
        MongoClient client = new MongoClient(new ServerAddress(host, port));
        return client
                .getDatabase(database)
                .withCodecRegistry(createCodecRegistry());
    }

    private CodecRegistry createCodecRegistry() {
        CodecProvider codecProvider = PojoCodecProvider.builder()
                .register(MODEL_PACKAGE)
                .build();
        return fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(codecProvider));
    }

    @Bean
    public MongoCollection<User> userCollection() {
        return mongoDatabase().getCollection("user", User.class);
    }

    @Bean
    public MongoCollection<Queue> queueCollection() {
        return mongoDatabase().getCollection("queue", Queue.class);
    }
}

































































































