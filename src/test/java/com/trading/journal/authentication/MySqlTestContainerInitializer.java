package com.trading.journal.authentication;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import static java.lang.String.format;

public class MySqlTestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(MySqlTestContainerInitializer.class);
    protected static final MySQLContainer<?> container;

    static {
        container = new MySQLContainer<>("mysql:5.6.51")
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
                format("spring.datasource.url=jdbc:mysql://%s:%s/trade-journal", container.getHost(), container.getMappedPort(3306)));

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                "spring.datasource.username=trade-journal");

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                "spring.datasource.password=trade-journal");

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext, "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver");
    }
}
