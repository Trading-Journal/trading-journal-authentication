package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.WithCustomMockUser;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import com.trading.journal.authentication.user.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
@WithCustomMockUser(authorities = {"ROLE_ADMIN"})
class TenanciesControllerTest {

    public static final String PATH_ID = "/admin/tenancies/{id}";

    public static final String PATH_LIMIT = "/admin/tenancies/{id}/limit/{limit}";

    @Autowired
    TenancyRepository tenancyRepository;

    private static WebTestClient webTestClient;

    @BeforeAll
    public static void setUp(@Autowired WebApplicationContext applicationContext, @Autowired TenancyRepository tenancyRepository) {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext).build();

        Stream<String> tenacies = Stream.of(
                "andyjohnson", "angelduncan", "angelowells", "arthurlawrence", "bernardmyers", "bethguzman", "blakecoleman", "brianmann", "cameronfleming", "carltonsantos",
                "carrietate", "catherinejones", "cecilperkins", "colinward", "conradhernandez", "doloreswilliamson", "dorisparker", "earlnorris", "eddiemassey", "elenaboyd",
                "elisavargas", "ermablack", "ernestinesteele", "ernestokim", "fanniehines", "gabrieldixon", "garylogan", "gerardwebb", "idagarza", "isaacjames",
                "jeromepratt", "joeldunn", "juliecarson", "kathyoliver", "katrinahawkins", "larryrobbins", "laurieadams", "lorettastanley", "luketyler", "melindafields",
                "natasharivera", "norawaters", "pedrosullivan", "phyllisterry", "rochellegraves", "sabrinagarcia", "sadiedavis", "veralamb", "vernawilkins", "victorialuna"
        );
        tenacies.map(tenancy -> Tenancy.builder().name(tenancy).build())
                .forEach(tenancyRepository::save);
    }

    @AfterAll
    public static void shotDown(@Autowired TenancyRepository tenancyRepository, @Autowired UserRepository userRepository) {
        userRepository.deleteAll();
        tenancyRepository.deleteAll();
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
        Tenancy tenancy = tenancyRepository.save(Tenancy.builder().name("invalid-limit").userLimit(15).userUsage(11).build());
        assertThat(tenancy.getUserUsage()).isEqualTo(11);

        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_LIMIT)
                        .build(tenancy.getId(), 10))
                .accept(MediaType.APPLICATION_JSON)
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