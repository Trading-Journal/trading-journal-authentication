package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.email.EmailField;
import com.trading.journal.authentication.email.EmailRequest;
import com.trading.journal.authentication.email.service.EmailSender;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.jwt.service.JwtTokenProvider;
import com.trading.journal.authentication.password.ChangePassword;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.user.service.UserService;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationRepository;
import com.trading.journal.authentication.verification.VerificationStatus;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
public class AuthenticationControllerChangePasswordIntegratedTest {

    @Autowired
    private UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VerificationRepository verificationRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    VerificationEmailService verificationEmailService;

    @MockBean
    EmailSender emailSender;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        verificationRepository.deleteAll();
    }

    @Test
    @DisplayName("Create and send the password change request")
    void requestPasswordChange() {
        String email = "mail@mail.com";

        UserRegistration user = new UserRegistration(
                "allan",
                "weber",
                "allanweber",
                email,
                "dad231#$#4",
                "dad231#$#4");

        userService.createNewUser(user);
        User applicationUser = userRepository.findByEmail(email).orElse(null);
        assertThat(applicationUser).isNotNull();
        applicationUser.enable();
        applicationUser.verify();
        userRepository.save(applicationUser);

        doNothing().when(verificationEmailService).sendEmail(any(), any());

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/authentication/change-password/request")
                        .queryParam("email", email)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        Verification verification = verificationRepository.getByTypeAndEmail(VerificationType.CHANGE_PASSWORD, email).get();
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(verification.getHash()).isNotBlank();

        applicationUser = userRepository.findByEmail(email).orElse(null);
        assertThat(applicationUser).isNotNull();
        assertThat(applicationUser.getEnabled()).isTrue();
        assertThat(applicationUser.getVerified()).isFalse();
    }

    @Test
    @DisplayName("Create password change request when user do not exist return exception")
    void requestPasswordChangeException() {
        String email = "mail@mail.com";

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/authentication/change-password/request")
                        .queryParam("email", email)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User mail@mail.com does not exist")
                );

        Optional<Verification> verification = verificationRepository.getByTypeAndEmail(VerificationType.CHANGE_PASSWORD, email);
        assertThat(verification).isEmpty();

        verify(verificationEmailService, never()).sendEmail(any(), any());
    }

    @Test
    @DisplayName("Change password with invalid inputs return exception")
    void changePasswordInvalidInputs() {
        ChangePassword changePassword = new ChangePassword(null, null, null, null);

        webTestClient
                .post()
                .uri("/authentication/change-password")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(changePassword)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> {
                    assertThat(response.get("email")).isEqualTo("Email is required");
                    assertThat(response.get("hash")).isEqualTo("Hash is required");
                    assertThat(response.get("password")).matches(message -> message.equals("Password is required") || message.equals("Password is not valid"));
                    assertThat(response.get("confirmPassword")).isEqualTo("Password confirmation is required");
                });
    }

    @Test
    @DisplayName("Change password with invalid email return exception")
    void changePasswordInvalidEmail() {
        ChangePassword changePassword = new ChangePassword("mail", "null", "dad231#$#4", "dad231#$#4");

        webTestClient
                .post()
                .uri("/authentication/change-password")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(changePassword)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("email")).isEqualTo("Email is invalid")
                );
    }

    @Test
    @DisplayName("Change password with password and confirmation different return exception")
    void changePasswordInvalidPasswords() {
        ChangePassword changePassword = new ChangePassword("mail@email.com", "null", "dad231#$#4", "dad231#$#4123");

        webTestClient
                .post()
                .uri("/authentication/change-password")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(changePassword)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("changePassword")).isEqualTo("Password and confirmation must be equal")
                );
    }

    @Test
    @DisplayName("Change password with hash that does not exist return exception")
    void changePasswordInvalidHash() {
        TokenData tokenData = jwtTokenProvider.generateTemporaryToken("mail@email.com");
        ChangePassword changePassword = new ChangePassword("mail@email.com", tokenData.token(), "dad231#$#4", "dad231#$#4");

        webTestClient
                .post()
                .uri("/authentication/change-password")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(changePassword)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("Request is invalid")
                );
    }

    @Test
    @DisplayName("Change password with hash for another email return exception")
    void changePasswordInvalidHashEmail() {
        TokenData tokenData = jwtTokenProvider.generateTemporaryToken("anotheremail@email.com");

        verificationRepository.save(Verification.builder()
                .hash(tokenData.token())
                .email("anotheremail@email.com")
                .type(VerificationType.CHANGE_PASSWORD)
                .status(VerificationStatus.PENDING)
                .lastChange(LocalDateTime.now())
                .build()
        );

        ChangePassword changePassword = new ChangePassword("mail@email.com", tokenData.token(), "dad231#$#4", "dad231#$#4");

        webTestClient
                .post()
                .uri("/authentication/change-password")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(changePassword)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("Change password request is invalid")
                );
    }

    @Test
    @DisplayName("Change password")
    void changePassword() {
        String email = "mail@email.com";
        UserRegistration user = new UserRegistration(
                "allan",
                "weber",
                "allanweber",
                email,
                "dad231#$#4",
                "dad231#$#4");

        userService.createNewUser(user);
        User applicationUser = userRepository.findByEmail(email).orElse(null);
        assertThat(applicationUser).isNotNull();
        applicationUser.enable();
        applicationUser.unproven();
        userRepository.save(applicationUser);

        TokenData tokenData = jwtTokenProvider.generateTemporaryToken(email);
        verificationRepository.save(Verification.builder()
                .hash(tokenData.token())
                .email(email)
                .type(VerificationType.CHANGE_PASSWORD)
                .status(VerificationStatus.PENDING)
                .lastChange(LocalDateTime.now())
                .build()
        );

        ChangePassword changePassword = new ChangePassword(email, tokenData.token(), "&UeK0j@tYRnhVGS&S64d", "&UeK0j@tYRnhVGS&S64d");

        String oldPasswordEncoded = userRepository.findByEmail(email).get().getPassword();
        assertThat(oldPasswordEncoded).isNotBlank();


        EmailRequest emailRequest = new EmailRequest(
                "Confirmação de alteração senha",
                "mail/change-password-confirmation.html",
                singletonList(new EmailField("$NAME", "allan weber")),
                singletonList(email)
        );
        doNothing().when(emailSender).send(emailRequest);

        webTestClient
                .post()
                .uri("/authentication/change-password")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(changePassword)
                .exchange()
                .expectStatus()
                .isOk();

        List<Verification> verifications = verificationRepository.findAll();
        assertThat(verifications).isEmpty();

        String newPasswordEncoded = userRepository.findByEmail(email).get().getPassword();
        assertThat(newPasswordEncoded).isNotBlank();

        assertThat(oldPasswordEncoded).isNotEqualTo(newPasswordEncoded);

        applicationUser = userRepository.findByEmail(email).orElse(null);
        assertThat(applicationUser).isNotNull();
        assertThat(applicationUser.getVerified()).isTrue();
    }
}
