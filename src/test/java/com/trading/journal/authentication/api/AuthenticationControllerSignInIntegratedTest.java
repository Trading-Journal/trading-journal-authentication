package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.email.service.EmailSender;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.user.service.ApplicationUserService;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
public class AuthenticationControllerSignInIntegratedTest {

    @Autowired
    private ApplicationUserService applicationUserService;

    @Autowired
    ApplicationUserRepository applicationUserRepository;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    EmailSender emailSender;

    @BeforeEach
    public void setUp() {
        applicationUserRepository.deleteAll();
        doNothing().when(emailSender).send(any());
    }

    @Test
    @DisplayName("When signIn user return success and token")
    void signIn() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "dad231#$#4",
                "dad231#$#4");

        applicationUserService.createNewUser(userRegistration);

        Login login = new Login("mail@mail.com", "dad231#$#4");

        webTestClient
                .post()
                .uri("/authentication/signin")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(login)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(LoginResponse.class)
                .value(response -> {
                    assertThat(response.accessToken()).isNotBlank();
                    assertThat(response.refreshToken()).isNotBlank();
                });
    }

    @Test
    @DisplayName("When signIn user that does not exist, return 401")
    void signInFails() {
        Login login = new Login("mail3@mail.com", "dad231#$#4");

        webTestClient
                .post()
                .uri("/authentication/signin")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(login)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("error")).isEqualTo("Bad Credentials"));
    }

    @Test
    @DisplayName("When signIn with wrong password, return 401")
    void signInFailsPassword() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName4",
                "mail4@mail.com",
                "dad231#$#4",
                "dad231#$#4");

        applicationUserService.createNewUser(userRegistration);

        Login login = new Login("mail4@mail.com", "wrong_password");

        webTestClient
                .post()
                .uri("/authentication/signin")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(login)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("error")).isEqualTo("Bad Credentials"));
    }

    @Test
    @DisplayName("When signIn fails because inputs are invalid")
    void signInFailsInvalidInput() {
        Login loginNullEmail = new Login(null, "dad231#$#4");
        webTestClient
                .post()
                .uri("/authentication/signin")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(loginNullEmail)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("email")).isEqualTo("Email is required"));

        Login loginNullPassword = new Login("mail@mail.com", null);
        webTestClient
                .post()
                .uri("/authentication/signin")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(loginNullPassword)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("password")).isEqualTo("Password is required"));

        Login loginInvalidEmail = new Login("password", "dad231#$#4");
        webTestClient
                .post()
                .uri("/authentication/signin")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(loginInvalidEmail)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("email")).isEqualTo("Email is invalid"));
    }
}
