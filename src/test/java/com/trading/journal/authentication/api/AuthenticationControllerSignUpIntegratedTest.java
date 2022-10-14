package com.trading.journal.authentication.api;

import com.trading.journal.authentication.PostgresTestContainerInitializer;
import com.trading.journal.authentication.email.service.EmailSender;
import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import com.trading.journal.authentication.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
public class AuthenticationControllerSignUpIntegratedTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TenancyRepository tenancyRepository;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    EmailSender emailSender;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        tenancyRepository.deleteAll();
        doNothing().when(emailSender).send(any());
    }

    @Test
    @DisplayName("When signUp as new user return success and the UserAuthority entity has AuthorityId because the authorities are static")
    void signUp() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "firstName",
                "lastName",
                "UserName2",
                "mail2@mail.com",
                "dad231#$#4",
                "dad231#$#4",
                false
        );

        webTestClient
                .post()
                .uri("/authentication/signup")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userRegistration)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(SignUpResponse.class)
                .value(response -> {
                    assertThat(response.email()).isEqualTo("mail2@mail.com");
                    assertThat(response.enabled()).isTrue();
                });
    }

    @Test
    @DisplayName("When signUp as new user with invalid input return error for the invalid input")
    void signUpInvalidInput() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );

        webTestClient
                .post()
                .uri("/authentication/signup")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userRegistration)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String,  List<String>>>() {
                })
                .value(response -> {
                    assertThat(response.get("errors")).contains("First name is required");
                    assertThat(response.get("errors")).contains("Last name is required");
                    assertThat(response.get("errors")).matches(message -> message.contains("Password is required") || message.contains("Password is not valid"));
                    assertThat(response.get("errors")).contains("Password confirmation is required");
                    assertThat(response.get("errors")).contains("User name is required");
                    assertThat(response.get("errors")).contains("Email is required");
                });
    }

    @Test
    @DisplayName("When signUp as new user with invalid email return error for the invalid input")
    void signUpInvalidEmail() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "firstName",
                "lastName",
                "UserName2",
                "mail2",
                "dad231#$#4",
                "dad231#$#4",
                false
        );

        webTestClient
                .post()
                .uri("/authentication/signup")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userRegistration)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String,  List<String>>>() {
                })
                .value(response ->
                        assertThat(response.get("errors")).contains("Email is invalid")
                );
    }

    @Test
    @DisplayName("When signUp as new user with password and confirmation different return error for the invalid input")
    void signUpInvalidPasswords() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "firstName",
                "lastName",
                "UserName2",
                "mail2@email.com",
                "dad231#$#4",
                "dad231#$#4xxx",
                false
        );

        webTestClient
                .post()
                .uri("/authentication/signup")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userRegistration)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, List<String>>>() {
                })
                .value(response ->
                        assertThat(response.get("errors")).contains("Password and confirmation must be equal")
                );
    }
}
