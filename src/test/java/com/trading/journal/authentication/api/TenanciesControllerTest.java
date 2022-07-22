package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.TestLoader;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.authentication.service.AuthenticationService;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
class TenanciesControllerTest {

    public static final String PATH_ID = "/admin/tenancies/{id}";

    public static final String PATH_LIMIT = "/admin/tenancies/{id}/limit/{limit}";
    private static String token;

    @Autowired
    TenancyRepository tenancyRepository;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    public static void setUp(
            @Autowired UserRepository userRepository,
            @Autowired PasswordEncoder encoder,
            @Autowired AuthenticationService authenticationService,
            @Autowired UserAuthorityService userAuthorityService,
            @Autowired TenancyRepository tenancyRepository
    ) {
        userRepository.deleteAll();
        TestLoader.load50Tenancies(tenancyRepository);
        User user = User.builder()
                .userName("johnwick")
                .password(encoder.encode("dad231#$#4"))
                .firstName("John")
                .lastName("Wick")
                .email("johnwick@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .build();
        User applicationUser = userRepository.save(user);
        userAuthorityService.saveAdminUserAuthorities(applicationUser);

        Login login = new Login("johnwick@mail.com", "dad231#$#4");
        LoginResponse loginResponse = authenticationService.signIn(login);
        assertThat(loginResponse).isNotNull();
        token = loginResponse.accessToken();
    }

    @DisplayName("Get tenancy by id")
    @Test
    void getUserById() {
        Tenancy tenancy = tenancyRepository.findByName("cecilperkins").orElse(Tenancy.builder().build());

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_ID)
                        .build(tenancy.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Tenancy.class)
                .value(response -> {
                    assertThat(response.getName()).isEqualTo("cecilperkins");
                    assertThat(response.getUserLimit()).isEqualTo(1);
                    assertThat(response.getUserUsage()).isEqualTo(0);
                });
    }

    @DisplayName("Get tenancy by id not found")
    @Test
    void getUserByIdNotFound() {
        long id = 1000L;
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_ID)
                        .build(id))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("Tenancy id not found")
                );
    }

    @DisplayName("Disable tenancy")
    @Test
    void disable() {
        Tenancy tenancy = tenancyRepository.findByName("arthurlawrence").orElse(Tenancy.builder().build());
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_ID)
                        .pathSegment("disable")
                        .build(tenancy.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        Tenancy tenancyDisabled = tenancyRepository.findByName("arthurlawrence").orElse(Tenancy.builder().build());
        assertThat(tenancyDisabled.getEnabled()).isFalse();
    }

    @DisplayName("Disable tenancy by id not found")
    @Test
    void disableNotFound() {
        long id = 1000L;
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_ID)
                        .pathSegment("disable")
                        .build(id))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("Tenancy id not found")
                );
    }

    @DisplayName("Enable tenancy")
    @Test
    void enable() {
        Tenancy tenancy = tenancyRepository.findByName("kathyoliver").orElse(Tenancy.builder().build());
        tenancy.disable();
        tenancyRepository.save(tenancy);
        assertThat(tenancy.getEnabled()).isFalse();

        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_ID)
                        .pathSegment("enable")
                        .build(tenancy.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        Tenancy tenancyDisabled = tenancyRepository.findByName("kathyoliver").orElse(Tenancy.builder().build());
        assertThat(tenancyDisabled.getEnabled()).isTrue();
    }

    @DisplayName("Enable tenancy by id not found")
    @Test
    void enableNotFound() {
        long id = 1000L;
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_ID)
                        .pathSegment("enable")
                        .build(id))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("Tenancy id not found")
                );
    }

    @DisplayName("Set new tenancy limit")
    @Test
    void limit() {
        Tenancy tenancy = tenancyRepository.findByName("phyllisterry").orElse(Tenancy.builder().build());
        assertThat(tenancy.getUserLimit()).isEqualTo(1);

        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_LIMIT)
                        .build(tenancy.getId(), 10))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Tenancy.class)
                .value(response -> {
                    assertThat(response.getName()).isEqualTo("phyllisterry");
                    assertThat(response.getUserLimit()).isEqualTo(10);
                    assertThat(response.getUserUsage()).isEqualTo(0);
                });

        Tenancy tenancyDisabled = tenancyRepository.findByName("phyllisterry").orElse(Tenancy.builder().build());
        assertThat(tenancyDisabled.getUserLimit()).isEqualTo(10);
    }

    @DisplayName("Set new tenancy limit by id not found")
    @Test
    void limitNotFound() {
        long id = 1000L;
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_LIMIT)
                        .build(id, 10))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("Tenancy id not found")
                );
    }

    @DisplayName("Set new tenancy limit exception when limit is lower than current usage")
    @Test
    void limitUsageError() {
        Tenancy tenancy =  tenancyRepository.save(Tenancy.builder().name("invalid-limit").userLimit(15).userUsage(11).build());
        assertThat(tenancy.getUserUsage()).isEqualTo(11);

        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_LIMIT)
                        .build(tenancy.getId(), 10))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("New tenancy limit is lower than the current usage")
                );
    }
}