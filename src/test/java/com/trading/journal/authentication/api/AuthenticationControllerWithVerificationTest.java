package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.service.ApplicationUserRepository;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationStatus;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import com.trading.journal.authentication.verification.service.VerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
@TestPropertySource(properties = {"journal.authentication.verification.enabled=true"})
public class AuthenticationControllerWithVerificationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    ApplicationUserRepository applicationUserRepository;

    @Autowired
    VerificationRepository verificationRepository;

    @MockBean
    VerificationEmailService verificationEmailService;

    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
        applicationUserRepository.deleteAll().block();
        verificationRepository.deleteAll().block();
    }

    @Test
    @DisplayName("When signUp as new user with verification enabled user must be created disabled")
    void signUp() {

        when(verificationEmailService.sendEmail(any(), any())).thenReturn(Mono.empty());

        UserRegistration userRegistration = new UserRegistration(
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

        Mono<Verification> verificationMono = verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, "mail2@mail.com");

        StepVerifier.create(verificationMono)
                .assertNext(verification -> {
                    assertThat(verification.getHash()).isNotBlank();
                    assertThat(verification.getStatus()).isEqualTo(VerificationStatus.PENDING);
                })
                .verifyComplete();

        Mono<ApplicationUser> applicationUserMono = applicationUserRepository.findByEmail("mail2@mail.com");
        StepVerifier.create(applicationUserMono)
                .assertNext(applicationUser -> {
                    assertThat(applicationUser.getEnabled()).isFalse();
                    assertThat(applicationUser.getVerified()).isFalse();
                })
                .verifyComplete();
    }
}
