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
    @Value("${mongo.host}")
    private String host;

    @Value("${mongo.port}")
    private Integer port;

    @Bean
    public MongoDatabase mongoDatabase() {
        CodecProvider codecProvider = PojoCodecProvider.builder()
                .register("co.axelrod.foodqueue.model")
                .build();

        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                fromProviders(codecProvider));

        MongoClient client = new MongoClient(new ServerAddress(host, port));

        return client
                .getDatabase("foodQueue")
                .withCodecRegistry(pojoCodecRegistry);
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

































































































