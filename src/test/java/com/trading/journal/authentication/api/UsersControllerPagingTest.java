package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.TestLoader;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.authentication.service.AuthenticationService;
import com.trading.journal.authentication.authority.service.AuthorityService;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;
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

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
class UsersControllerPagingTest {

    public static final String PATH = "/admin/users";
    private static String token;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    public static void setUp(
            @Autowired ApplicationUserRepository applicationUserRepository,
            @Autowired UserAuthorityRepository userAuthorityRepository,
            @Autowired AuthorityService authorityService,
            @Autowired PasswordEncoder encoder,
            @Autowired AuthenticationService authenticationService,
            @Autowired UserAuthorityService userAuthorityService
    ) {
        TestLoader.load50Users(applicationUserRepository, userAuthorityRepository, authorityService);

        ApplicationUser user = ApplicationUser.builder()
                .userName("johnwick")
                .password(encoder.encode("dad231#$#4"))
                .firstName("John")
                .lastName("Wick")
                .email("johnwick@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .build();
        ApplicationUser applicationUser = applicationUserRepository.save(user);
        userAuthorityService.saveAdminUserAuthorities(applicationUser);

        Login login = new Login("johnwick@mail.com", "dad231#$#4");
        LoginResponse loginResponse = authenticationService.signIn(login);
        assertThat(loginResponse).isNotNull();
        token = loginResponse.accessToken();
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
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(10);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(6);
                    assertThat(response.totalItems()).isEqualTo(51L);
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
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(10);
                    assertThat(response.currentPage()).isEqualTo(3);
                    assertThat(response.totalPages()).isEqualTo(6);
                    assertThat(response.totalItems()).isEqualTo(51L);
                    assertThat(response.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                            .containsExactly("Jerome Pratt", "Joel Dunn", "Julie Carson", "Kathy Oliver", "Katrina Hawkins", "Larry Robbins", "Laurie Adams", "Loretta Stanley", "Luke Tyler", "Melinda Fields");
                });
    }

    @DisplayName("Page out of range return empty results\"")
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
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(0);
                    assertThat(response.currentPage()).isEqualTo(6);
                    assertThat(response.totalPages()).isEqualTo(6);
                    assertThat(response.totalItems()).isEqualTo(51L);
                });
    }

    @DisplayName("Page out of range return empty results")
    @Test
    void plainSort() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("sort", "firstName", "desc")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(10);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(6);
                    assertThat(response.totalItems()).isEqualTo(51L);
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
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(10);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.totalPages()).isEqualTo(6);
                    assertThat(response.totalItems()).isEqualTo(51L);
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
                .header("Authorization", "Bearer " + token)
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
                .header("Authorization", "Bearer " + token)
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
                .header("Authorization", "Bearer " + token)
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
                .header("Authorization", "Bearer " + token)
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
                .header("Authorization", "Bearer " + token)
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
                .header("Authorization", "Bearer " + token)
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