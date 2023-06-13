package com.trading.journal.authentication;

import com.allanweber.jwttoken.data.JwtProperties;
import com.trading.journal.authentication.user.properties.AdminUserProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableConfigurationProperties({AdminUserProperties.class, JwtProperties.class})
@EnableJpaRepositories
//@ImportRuntimeHints(DatabaseRuntimeHintsRegistrar.class)
public class AuthenticationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationApplication.class, args);
    }
}
