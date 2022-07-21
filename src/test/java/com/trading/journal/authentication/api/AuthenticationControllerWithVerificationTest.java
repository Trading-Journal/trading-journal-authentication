package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationStatus;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import com.trading.journal.authentication.verification.VerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
@TestPropertySource(properties = {"journal.authentication.verification.enabled=true"})
public class AuthenticationControllerWithVerificationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TenancyRepository tenancyRepository;

    @Autowired
    VerificationRepository verificationRepository;

    @MockBean
    VerificationEmailService verificationEmailService;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        verificationRepository.deleteAll();
        tenancyRepository.deleteAll();
        doNothing().when(verificationEmailService).sendEmail(any(), any());
    }

    @Test
    @DisplayName("When signUp as new user with verification enabled user must be created disabled")
    void signUp() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "firstName",
                "lastName",
                "UserName2",
                "mail2@mail.com",
                "dad231#$#4",
                "dad231#$#4");

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
                    assertThat(response.enabled()).isFalse();
                });

        Verification verification = verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, "mail2@mail.com").get();
        assertThat(verification.getHash()).isNotBlank();
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.PENDING);

        User applicationUser = userRepository.findByEmail("mail2@mail.com").get();
        assertThat(applicationUser.getEnabled()).isFalse();
        assertThat(applicationUser.getVerified()).isFalse();
    }

    @Test
    @DisplayName("Receive the verification URL and verify the user")
    void verifyUser() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "firstName",
                "lastName",
                "UserName2",
                "mail2@mail.com",
                "dad231#$#4",
                "dad231#$#4");

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
                    assertThat(response.enabled()).isFalse();
                });

        Verification verification = verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, "mail2@mail.com").get();

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/authentication/verify")
                        .queryParam("hash", verification.getHash())
                        .build())
                .exchange()
                .expectStatus()
                .isOk();

        Optional<Verification> verificationNull = verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, "mail2@mail.com");
        assertThat(verificationNull).isEmpty();

        User applicationUser = userRepository.findByEmail("mail2@mail.com").get();
        assertThat(applicationUser.getEnabled()).isTrue();
        assertThat(applicationUser.getVerified()).isTrue();
    }

    @Test
    @DisplayName("Receive the verification URL, request another verification code and verify the user")
    void verifyUserWithSecondVerification() throws InterruptedException {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "firstName",
                "lastName",
                "UserName2",
                "mail2@mail.com",
                "dad231#$#4",
                "dad231#$#4");

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
                    assertThat(response.enabled()).isFalse();
                });

        List<Verification> verifications = verificationRepository.findAll();
        assertThat(verifications).hasSize(1);

        Verification verificationByEmail = verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, "mail2@mail.com").get();
        String firstHash = verificationByEmail.getHash();
        Thread.sleep(1000);// if it runs right way generated the same hash

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/authentication/verify/send")
                        .queryParam("email", "mail2@mail.com")
                        .build())
                .exchange()
                .expectStatus()
                .isOk();

        verifications = verificationRepository.findAll();
        assertThat(verifications).hasSize(1);
        Verification secondVerificationByEmail = verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, "mail2@mail.com").get();
        String secondHash = secondVerificationByEmail.getHash();

        assertThat(firstHash).isNotEqualTo(secondHash);

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/authentication/verify")
                        .queryParam("hash", secondHash)
                        .build())
                .exchange()
                .expectStatus()
                .isOk();

        Optional<Verification> verificationNull = verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, "mail2@mail.com");
        assertThat(verificationNull).isEmpty();

        User applicationUser = userRepository.findByEmail("mail2@mail.com").get();
        assertThat(applicationUser.getEnabled()).isTrue();
        assertThat(applicationUser.getVerified()).isTrue();
    }
}
