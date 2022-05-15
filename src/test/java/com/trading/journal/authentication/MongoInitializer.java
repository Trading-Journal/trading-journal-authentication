package com.trading.journal.authentication;

import static java.lang.String.format;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MongoDBContainer;

public class MongoInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    protected static final MongoDBContainer mongoContainer;
    static {
        mongoContainer = new MongoDBContainer("mongo:4.2.10").withReuse(true);
        mongoContainer.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                format("spring.data.mongodb.uri=mongodb://%s:%s", mongoContainer.getHost(),
                        mongoContainer.getMappedPort(27017)));
    }
}
