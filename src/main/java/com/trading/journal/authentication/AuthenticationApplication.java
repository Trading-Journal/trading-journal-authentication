package com.trading.journal.authentication;

import com.trading.journal.authentication.authority.properties.AuthorityProperties;
import com.trading.journal.authentication.configuration.properties.DatasourceProperties;
import com.trading.journal.authentication.user.properties.AdminUserProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({DatasourceProperties.class, AuthorityProperties.class, AdminUserProperties.class})
public class AuthenticationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationApplication.class, args);
    }
}
