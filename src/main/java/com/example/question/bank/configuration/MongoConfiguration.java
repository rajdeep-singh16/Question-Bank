package com.example.question.bank.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;

@Configuration
public class MongoConfiguration extends AbstractReactiveMongoConfiguration {
//    String URI = "mongodb://localhost:27017";
    String URI = "";
    String DB_NAME = "questionBank";

    @Override
    public MongoClient reactiveMongoClient(){
        ConnectionString connectionString = new ConnectionString(URI);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    @Override
    protected String getDatabaseName() {
        return DB_NAME;
    }

    @Bean
    public ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory(){
        return new SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), getDatabaseName());
    }
    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(){
        return new ReactiveMongoTemplate(reactiveMongoDatabaseFactory());
    }
}
