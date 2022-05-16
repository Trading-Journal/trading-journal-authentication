package com.trading.journal.authentication;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MongoInitializer.class)
class AuthenticationApplicationTests {

    @DisplayName("Test the spring context load for all beans, with they are correctly configured")
    @Test
    void contextLoads() {
    }

}
