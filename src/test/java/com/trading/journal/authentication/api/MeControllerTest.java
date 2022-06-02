package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.authentication.service.AuthenticationService;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.user.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
public class MeControllerTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApplicationUserService applicationUserService;

    @Autowired
    private AuthenticationService authenticationService;

    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
    }

    @DisplayName("When logged user hit Me endpoint, return its information")
    @ParameterizedTest
    @MethodSource("feedUsers")
    void meEndpoint(UserRegistration user) {
        applicationUserService.createNewUser(user).block();

        Login login = new Login(user.email(), user.password());

        LoginResponse loginResponse = authenticationService.signIn(login).block();

        assert loginResponse != null;
        webTestClient
                .get()
                .uri("/me")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + loginResponse.accessToken())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getUserName()).isEqualTo(user.userName());
                    assertThat(response.getFirstName()).isEqualTo(user.firstName());
                    assertThat(response.getLastName()).isEqualTo(user.lastName());
                    assertThat(response.getEmail()).isEqualTo(user.email());
                });
    }

    private static Stream<UserRegistration> feedUsers() {
        return Stream.of(
                new UserRegistration(
                        "John",
                        "Wick",
                        "johnwick",
                        "johnwick@mail.com",
                        "dad231#$#4",
                        "dad231#$#4"),
                new UserRegistration(
                        "John",
                        "Rambo",
                        "johnrambo",
                        "johnrambo@mail.com",
                        "dad231#$#4",
                        "dad231#$#4"),
                new UserRegistration(
                        "Han",
                        "Solo ",
                        "hansolo",
                        "hansolo@mail.com",
                        "dad231#$#4",
                        "dad231#$#4"));
    }
}
