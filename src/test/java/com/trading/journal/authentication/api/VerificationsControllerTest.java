package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.WithCustomMockUser;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.verification.*;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
@WithCustomMockUser(authorities = {"ROLE_ADMIN"})
class VerificationsControllerTest {

    public static final String PATH_EMAIL = "/admin/verifications/{email}";

    public static final String PATH = "/admin/verifications";

    @Autowired
    VerificationRepository verificationRepository;

    @Autowired
    UserRepository userRepository;

    @MockBean
    VerificationEmailService verificationEmailService;

    private static WebTestClient webTestClient;

    @BeforeAll
    public static void setUp(@Autowired WebApplicationContext applicationContext) {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @BeforeEach
    public void setUp() {
        verificationRepository.deleteAll();
        doNothing().when(verificationEmailService).sendEmail(any(), any());
    }

    @AfterAll
    public static void shotDown(@Autowired VerificationRepository verificationRepository, @Autowired UserRepository userRepository) {
        verificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("Get All verification by email")
    @Test
    void getAll() {
        String email = "mail@mail.com";

        verificationRepository.save(Verification.builder()
                .email(email)
                .type(VerificationType.REGISTRATION)
                .hash(UUID.randomUUID().toString())
                .lastChange(LocalDateTime.now())
                .status(VerificationStatus.DONE)
                .build());

        verificationRepository.save(Verification.builder()
                .email(email)
                .type(VerificationType.CHANGE_PASSWORD)
                .hash(UUID.randomUUID().toString())
                .lastChange(LocalDateTime.now())
                .status(VerificationStatus.DONE)
                .build());

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_EMAIL)
                        .build(email))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Verification>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(2);
                    assertThat(response).extracting(Verification::getEmail).containsOnly(email);
                    assertThat(response).extracting(Verification::getType).containsExactlyInAnyOrder(VerificationType.REGISTRATION, VerificationType.CHANGE_PASSWORD);
                });
    }

    @DisplayName("Get All verification by email")
    @Test
    void getAllNoneFound() {
        String email = "mail@mail.com";
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_EMAIL)
                        .build(email))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Verification>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(0);
                });
    }

    @DisplayName("Create verification by email and type")
    @Test
    void create() {
        String email = "mail@mail.com";

        User user = User.builder()
                .userName("UserName")
                .password("encoded_password")
                .firstName("lastName")
                .lastName("Wick")
                .email(email)
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new VerificationRequest(email, VerificationType.CHANGE_PASSWORD))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Verification.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                });
    }

    @DisplayName("Create verification by email and type user not found return error")
    @Test
    void createError() {
        String email = "mail2@mail.com";

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new VerificationRequest(email, VerificationType.CHANGE_PASSWORD))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User mail2@mail.com does not exist")
                );
    }
}