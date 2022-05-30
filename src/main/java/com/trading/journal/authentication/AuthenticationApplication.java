package com.trading.journal.authentication;

import com.trading.journal.authentication.authority.properties.AuthorityProperties;
import com.trading.journal.authentication.configuration.DatasourceProperties;
import com.trading.journal.authentication.verification.properties.VerificationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication(exclude = {R2dbcAutoConfiguration.class})
@EnableWebFlux
@EnableConfigurationProperties({DatasourceProperties.class, AuthorityProperties.class, VerificationProperties.class})
@EnableR2dbcRepositories
public class AuthenticationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationApplication.class, args);
    }
}
