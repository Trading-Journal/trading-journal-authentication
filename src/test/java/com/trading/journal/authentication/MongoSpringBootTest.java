package com.trading.journal.authentication;

import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@Target(java.lang.annotation.ElementType.TYPE)
@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MongoInitializer.class)
public @interface MongoSpringBootTest {

}
