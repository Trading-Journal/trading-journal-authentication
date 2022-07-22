package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.TestLoader;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.authentication.service.AuthenticationService;
import com.trading.journal.authentication.pageable.PageResponse;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
class TenanciesControllerPagingTest {

    public static final String PATH = "/admin/tenancies";
    private static String token;

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

    @DisplayName("Get first page of all tenancies without any arguments")
    @Test
    void plainPageable() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
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
                .header("Authorization", "Bearer " + token)
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
                .header("Authorization", "Bearer " + token)
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
                .header("Authorization", "Bearer " + token)
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
                .header("Authorization", "Bearer " + token)
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
                            .containsExactly("andyjohnson","doloreswilliamson","juliecarson");
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
                .header("Authorization", "Bearer " + token)
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
                            .containsExactly("arthurlawrence","blakecoleman","ermablack","larryrobbins");
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
                .header("Authorization", "Bearer " + token)
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
                            .containsExactly("laurieadams","veralamb");
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
                .header("Authorization", "Bearer " + token)
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
                .header("Authorization", "Bearer " + token)
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
                            .containsExactly("veralamb","laurieadams","larryrobbins","ermablack");
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
                .header("Authorization", "Bearer " + token)
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
                            .containsExactly("blakecoleman","arthurlawrence");
                });
    }
}