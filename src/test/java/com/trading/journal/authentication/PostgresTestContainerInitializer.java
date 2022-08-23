package com.trading.journal.authentication;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import static java.lang.String.format;

public class PostgresTestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(PostgresTestContainerInitializer.class);
    protected static final PostgreSQLContainer<?> container;

    static {
        container = new PostgreSQLContainer<>("postgres:9.6.12")
                .withUrlParam("allowMultiQueries", "true")
                .withDatabaseName("trade-journal")
                .withUsername("trade-journal")
                .withPassword("trade-journal")
                .withInitScript("init_test_container_databases.sql")
                .withLogConsumer(new Slf4jLogConsumer(logger));
        container.start();
    }

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                format("spring.datasource.url=jdbc:postgresql://%s:%s/trade-journal", container.getHost(), container.getMappedPort(5432)));

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                "spring.datasource.username=trade-journal");

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                "spring.datasource.password=trade-journal");

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                "spring.datasource.driver-class-name=org.postgresql.Driver");

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                "spring.datasource.hikari.minimum-idle=5");
    }
}
