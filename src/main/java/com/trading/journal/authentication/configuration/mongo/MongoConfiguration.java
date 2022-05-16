package com.trading.journal.authentication.configuration.mongo;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

@Configuration
@EnableConfigurationProperties(MongoProperties.class)
public class MongoConfiguration extends AbstractReactiveMongoConfiguration {

    private final MongoProperties properties;

    public MongoConfiguration(MongoProperties properties) {
        super();
        this.properties = properties;
    }

    @Override
    protected String getDatabaseName() {
        return properties.getDatabase();
    }

    @Override
    public MongoClient reactiveMongoClient() {
        return MongoClients.create(properties.getUri());
    }
}
