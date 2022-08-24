package com.trading.journal.authentication.api;

import com.trading.journal.authentication.PostgresTestContainerInitializer;
import com.trading.journal.authentication.WithCustomMockUser;
import com.trading.journal.authentication.pageable.PageResponse;
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

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
@WithCustomMockUser(authorities = {"ROLE_ADMIN"})
class TenanciesControllerPagingTest {

    public static final String PATH = "/admin/tenancies";
    private static WebTestClient webTestClient;

    @BeforeAll
    public static void setUp(
            @Autowired WebApplicationContext applicationContext,
            @Autowired TenancyRepository tenancyRepository,
            @Autowired UserRepository userRepository
    ) {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext).build();

        userRepository.deleteAll();
        tenancyRepository.deleteAll();

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

    @DisplayName("Get first page of all tenancies without any arguments")
    @Test
    void plainPageable() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Tenancy>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(10);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(5);
                    assertThat(response.totalItems()).isEqualTo(50L);
                    assertThat(response.items()).extracting(Tenancy::getName)
                            .containsExactly("andyjohnson", "angelduncan", "angelowells", "arthurlawrence", "bernardmyers", "bethguzman", "blakecoleman", "brianmann", "cameronfleming", "carltonsantos");
                });
    }

    @DisplayName("Get items in the middle pages")
    @Test
    void middlePage() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("page", "3")
                        .queryParam("size", "10")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Tenancy>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(10);
                    assertThat(response.currentPage()).isEqualTo(3);
                    assertThat(response.totalPages()).isEqualTo(5);
                    assertThat(response.totalItems()).isEqualTo(50L);
                    assertThat(response.items()).extracting(Tenancy::getName)
                            .containsExactly("jeromepratt", "joeldunn", "juliecarson", "kathyoliver", "katrinahawkins", "larryrobbins", "laurieadams", "lorettastanley", "luketyler", "melindafields");
                });
    }

    @DisplayName("Page out of range return empty results")
    @Test
    void outOfRange() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("page", "5")
                        .queryParam("size", "10")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Tenancy>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(0);
                    assertThat(response.currentPage()).isEqualTo(5);
                    assertThat(response.totalPages()).isEqualTo(5);
                    assertThat(response.totalItems()).isEqualTo(50L);
                });
    }

    @DisplayName("Sort with one column")
    @Test
    void plainSort() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("sort", "name", "desc")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Tenancy>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(10);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(5);
                    assertThat(response.totalItems()).isEqualTo(50L);
                    assertThat(response.items()).extracting(Tenancy::getName)
                            .containsExactly("victorialuna", "vernawilkins", "veralamb", "sadiedavis", "sabrinagarcia", "rochellegraves", "phyllisterry", "pedrosullivan", "norawaters", "natasharivera");
                });
    }

    @DisplayName("Filter tenancy returning only one page")
    @Test
    void filterFirstPage() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("filter", "son")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Tenancy>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(3);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(1);
                    assertThat(response.totalItems()).isEqualTo(3L);
                    assertThat(response.items()).extracting(Tenancy::getName)
                            .containsExactly("andyjohnson", "doloreswilliamson", "juliecarson");
                });
    }

    @DisplayName("Filter tenancy returning two pages page")
    @Test
    void filterTwoPages() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("page", "0")
                        .queryParam("size", "4")
                        .queryParam("filter", "la")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Tenancy>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(4);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(2);
                    assertThat(response.totalItems()).isEqualTo(6L);
                    assertThat(response.items()).extracting(Tenancy::getName)
                            .containsExactly("arthurlawrence", "blakecoleman", "ermablack", "larryrobbins");
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("page", "1")
                        .queryParam("size", "4")
                        .queryParam("filter", "la")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Tenancy>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(2);
                    assertThat(response.currentPage()).isEqualTo(1);
                    assertThat(response.totalPages()).isEqualTo(2);
                    assertThat(response.totalItems()).isEqualTo(6L);
                    assertThat(response.items()).extracting(Tenancy::getName)
                            .containsExactly("laurieadams", "veralamb");
                });
    }

    @DisplayName("Filter tenancy returning no results")
    @Test
    void filterEmpty() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("filter", "www")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Tenancy>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(0);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(0);
                    assertThat(response.totalItems()).isEqualTo(0L);
                });
    }

    @DisplayName("Filter tenancy returning two pages page but sorting by name")
    @Test
    void filterAndSort() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("page", "0")
                        .queryParam("size", "4")
                        .queryParam("filter", "la")
                        .queryParam("sort", "name", "desc")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Tenancy>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(4);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(2);
                    assertThat(response.totalItems()).isEqualTo(6L);
                    assertThat(response.items()).extracting(Tenancy::getName)
                            .containsExactly("veralamb", "laurieadams", "larryrobbins", "ermablack");
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("page", "1")
                        .queryParam("size", "4")
                        .queryParam("filter", "la")
                        .queryParam("sort", "name", "desc")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<Tenancy>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(2);
                    assertThat(response.currentPage()).isEqualTo(1);
                    assertThat(response.totalPages()).isEqualTo(2);
                    assertThat(response.totalItems()).isEqualTo(6L);
                    assertThat(response.items()).extracting(Tenancy::getName)
                            .containsExactly("blakecoleman", "arthurlawrence");
                });
    }
}