package com.trading.journal.authentication.api;

import com.trading.journal.authentication.PostgresTestContainerInitializer;
import com.trading.journal.authentication.WithCustomMockUser;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserInfo;
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

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
@WithCustomMockUser(authorities = {"ROLE_ADMIN"})
class UsersControllerPagingTest {

    public static final String PATH = "/admin/users";

    private static WebTestClient webTestClient;

    private static Tenancy tenancy;

    @BeforeAll
    public static void setUp(
            @Autowired WebApplicationContext applicationContext,
            @Autowired TenancyRepository tenancyRepository,
            @Autowired UserRepository userRepository) {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext).build();
        tenancy = tenancyRepository.save(Tenancy.builder().name("tenancy").build());
        Stream<String> users = Stream.of(
                "Andy Johnson", "Angel Duncan", "Angelo Wells", "Arthur Lawrence", "Bernard Myers", "Beth Guzman", "Blake Coleman", "Brian Mann", "Cameron Fleming", "Carlton Santos",
                "Carrie Tate", "Catherine Jones", "Cecil Perkins", "Colin Ward", "Conrad Hernandez", "Dolores Williamson", "Doris Parker", "Earl Norris", "Eddie Massey", "Elena Boyd",
                "Elisa Vargas", "Erma Black", "Ernestine Steele", "Ernesto Kim", "Fannie Hines", "Gabriel Dixon", "Gary Logan", "Gerard Webb", "Ida Garza", "Isaac James",
                "Jerome Pratt", "Joel Dunn", "Julie Carson", "Kathy Oliver", "Katrina Hawkins", "Larry Robbins", "Laurie Adams", "Loretta Stanley", "Luke Tyler", "Melinda Fields",
                "Natasha Rivera", "Nora Waters", "Pedro Sullivan", "Phyllis Terry", "Rochelle Graves", "Sabrina Garcia", "Sadie Davis", "Vera Lamb", "Verna Wilkins", "Victoria Luna"
        );
        users.map(user -> {
            String userName = user.replace(" ", "").toLowerCase();
            String email = userName.concat("@email.com");
            String[] names = user.split(" ");
            String firstName = names[0];
            String lastName = names[1];
            return User.builder()
                    .userName(userName)
                    .email(email)
                    .password(UUID.randomUUID().toString())
                    .firstName(firstName)
                    .lastName(lastName)
                    .enabled(true)
                    .verified(true)
                    .createdAt(LocalDateTime.now())
                    .tenancy(tenancy)
                    .build();
        }).forEach(userRepository::save);
    }

    @AfterAll
    public static void shotDown(@Autowired UserRepository userRepository, @Autowired TenancyRepository tenancyRepository) {
        userRepository.deleteAll();
        tenancyRepository.deleteAll();
    }

    @DisplayName("Get first page of all users without any arguments")
    @Test
    void plainPageable() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(10);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(5);
                    assertThat(response.totalItems()).isEqualTo(50L);
                    assertThat(response.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                            .containsExactly("Andy Johnson", "Angel Duncan", "Angelo Wells", "Arthur Lawrence", "Bernard Myers", "Beth Guzman", "Blake Coleman", "Brian Mann", "Cameron Fleming", "Carlton Santos");
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
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(10);
                    assertThat(response.currentPage()).isEqualTo(3);
                    assertThat(response.totalPages()).isEqualTo(5);
                    assertThat(response.totalItems()).isEqualTo(50L);
                    assertThat(response.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                            .containsExactly("Jerome Pratt", "Joel Dunn", "Julie Carson", "Kathy Oliver", "Katrina Hawkins", "Larry Robbins", "Laurie Adams", "Loretta Stanley", "Luke Tyler", "Melinda Fields");
                });
    }

    @DisplayName("Page out of range return empty results")
    @Test
    void outOfRange() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("page", "6")
                        .queryParam("size", "10")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(0);
                    assertThat(response.currentPage()).isEqualTo(6);
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
                        .queryParam("sort", "firstName", "desc")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(10);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(5);
                    assertThat(response.totalItems()).isEqualTo(50L);
                    assertThat(response.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                            .containsExactly("Victoria Luna", "Verna Wilkins", "Vera Lamb", "Sadie Davis", "Sabrina Garcia", "Rochelle Graves", "Phyllis Terry", "Pedro Sullivan", "Nora Waters", "Natasha Rivera");
                });
    }

    @DisplayName("Sort for two columns")
    @Test
    void sortTwoColumns() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("sort", "firstName", "desc", "lastName", "asc")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(10);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(5);
                    assertThat(response.totalItems()).isEqualTo(50L);
                    assertThat(response.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                            .containsExactly("Victoria Luna", "Verna Wilkins", "Vera Lamb", "Sadie Davis", "Sabrina Garcia", "Rochelle Graves", "Phyllis Terry", "Pedro Sullivan", "Nora Waters", "Natasha Rivera");
                });
    }

    @DisplayName("Filter users returning only one page")
    @Test
    void filterFirstPage() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("filter", "son")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(3);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(1);
                    assertThat(response.totalItems()).isEqualTo(3L);
                    assertThat(response.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                            .containsExactly("Andy Johnson", "Dolores Williamson", "Julie Carson");
                });
    }

    @DisplayName("Filter users returning two pages page")
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
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(4);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(2);
                    assertThat(response.totalItems()).isEqualTo(6L);
                    assertThat(response.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                            .containsExactly("Arthur Lawrence", "Blake Coleman", "Erma Black", "Larry Robbins");
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
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(2);
                    assertThat(response.currentPage()).isEqualTo(1);
                    assertThat(response.totalPages()).isEqualTo(2);
                    assertThat(response.totalItems()).isEqualTo(6L);
                    assertThat(response.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                            .containsExactly("Laurie Adams", "Vera Lamb");
                });
    }

    @DisplayName("Filter users returning no results")
    @Test
    void filterEmpty() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("filter", "www")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(0);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(0);
                    assertThat(response.totalItems()).isEqualTo(0L);
                });
    }

    @DisplayName("Filter users returning two pages page but sorting by last name")
    @Test
    void filterAndSort() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("page", "0")
                        .queryParam("size", "4")
                        .queryParam("filter", "la")
                        .queryParam("sort", "lastName", "desc")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(4);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(2);
                    assertThat(response.totalItems()).isEqualTo(6L);
                    assertThat(response.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                            .containsExactly("Larry Robbins", "Arthur Lawrence", "Vera Lamb", "Blake Coleman");
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("page", "1")
                        .queryParam("size", "4")
                        .queryParam("filter", "la")
                        .queryParam("sort", "lastName", "desc")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(2);
                    assertThat(response.currentPage()).isEqualTo(1);
                    assertThat(response.totalPages()).isEqualTo(2);
                    assertThat(response.totalItems()).isEqualTo(6L);
                    assertThat(response.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                            .containsExactly("Erma Black", "Laurie Adams");
                });
    }
}