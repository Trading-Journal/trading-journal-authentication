package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.authority.service.UserAuthorityRepository;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.service.ApplicationUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
@TestPropertySource(properties = {"journal.authentication.authority.type=DATABASE"})
public class AuthenticationControllerDatabaseAuthoritiesTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    UserAuthorityRepository userAuthorityRepository;

    private WebTestClient webTestClient;

    @Autowired
    ApplicationUserRepository applicationUserRepository;

    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
        applicationUserRepository.deleteAll();
    }

    @Test
    @DisplayName("When signUp as new user return success and the UserAuthority entity has an id to Authority entity")
    void signUp() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName5",
                "mail5@mail.com",
                "dad231#$#4",
                "dad231#$#4");

        webTestClient
                .post()
                .uri("/authentication/signup")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userRegistration)
                .exchange()
                .expectStatus()
                .isOk();

        List<UserAuthority> userAuthorities = userAuthorityRepository.findAll();
        assert userAuthorities != null;
        userAuthorities.forEach(userAuthority -> assertThat(userAuthority.getAuthorityId()).isNotNull());
    }
}
