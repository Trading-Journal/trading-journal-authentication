package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.TestLoader;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.authentication.service.AuthenticationService;
import com.trading.journal.authentication.authority.service.AuthorityService;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
class OrganisationUsersControllerTest {

    public static final String PATH = "/organisation/users";

    public static final String PATH_BY_ID = "/organisation/users/{id}";
    private static String token;

    private static String tenancy;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    UserRepository userRepository;

    @BeforeAll
    public static void setUp(
            @Autowired UserRepository userRepository,
            @Autowired UserAuthorityRepository userAuthorityRepository,
            @Autowired AuthorityService authorityService,
            @Autowired PasswordEncoder encoder,
            @Autowired AuthenticationService authenticationService,
            @Autowired UserAuthorityService userAuthorityService,
            @Autowired TenancyRepository tenancyRepository
    ) {
        TestLoader.load50Users(userRepository, userAuthorityRepository, authorityService, tenancyRepository);
        Tenancy tenancy1 = tenancyRepository.findByName("test").get();
        tenancy = tenancy1.getId().toString();
        User user = User.builder()
                .userName("johnwick")
                .password(encoder.encode("dad231#$#4"))
                .firstName("John")
                .lastName("Wick")
                .email("johnwick@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .tenancy(tenancy1)
                .build();
        User applicationUser = userRepository.save(user);
        userAuthorityService.saveCommonUserAuthorities(applicationUser);
        userAuthorityService.saveOrganisationAdminUserAuthorities(applicationUser);

        Login login = new Login("johnwick@mail.com", "dad231#$#4");
        LoginResponse loginResponse = authenticationService.signIn(login);
        assertThat(loginResponse).isNotNull();
        token = loginResponse.accessToken();
    }

    @AfterAll
    public static void shotDown(@Autowired UserRepository userRepository,
                                @Autowired UserAuthorityRepository userAuthorityRepository,
                                @Autowired TenancyRepository tenancyRepository) {
        userRepository.deleteAll();
        userAuthorityRepository.deleteAll();
        tenancyRepository.deleteAll();
    }

    @DisplayName("Page organisation users")
    @Test
    void page() {
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
                    assertThat(response.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                            .containsExactly("Andy Johnson", "Angel Duncan", "Angelo Wells", "Arthur Lawrence", "Bernard Myers", "Beth Guzman", "Blake Coleman", "Brian Mann", "Cameron Fleming", "Carlton Santos");
                });

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
                });

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
                    assertThat(response.items()).hasSize(1);
                    assertThat(response.currentPage()).isEqualTo(1);
                    assertThat(response.totalPages()).isEqualTo(2);
                });
    }

    @DisplayName("Get user by id")
    @Test
    void getUserById() {
        long userId = getUserId("ermablack@email.com");

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getFirstName()).isEqualTo("Erma");
                    assertThat(response.getLastName()).isEqualTo("Black");
                    assertThat(response.getEmail()).isEqualTo("ermablack@email.com");
                    assertThat(response.getUserName()).isEqualTo("ermablack");
                    assertThat(response.getVerified()).isEqualTo(true);
                    assertThat(response.getEnabled()).isEqualTo(true);
                });
    }

    @DisplayName("Get user by id not found")
    @Test
    void getUserByIdNotFound() {
        long userId = 1000L;
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User id not found")
                );
    }

    @DisplayName("Disable user by id")
    @Test
    void disableUserById() {
        long userId = getUserId("ernestokim@email.com");

        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("disable")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getFirstName()).isEqualTo("Ernesto");
                    assertThat(response.getLastName()).isEqualTo("Kim");
                    assertThat(response.getEmail()).isEqualTo("ernestokim@email.com");
                    assertThat(response.getUserName()).isEqualTo("ernestokim");
                    assertThat(response.getVerified()).isEqualTo(true);
                    assertThat(response.getEnabled()).isEqualTo(false);
                });
    }

    @DisplayName("Disable user by id not found")
    @Test
    void disableUserByIdNotFound() {
        long userId = 1000L;
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("disable")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User id not found")
                );
    }

    @DisplayName("Enable user by id")
    @Test
    void enableUserById() {
        User applicationUser = userRepository.findByEmail("fanniehines@email.com").get();
        assertThat(applicationUser).isNotNull();
        long userId = applicationUser.getId();

        applicationUser.disable();
        applicationUser = userRepository.save(applicationUser);
        assertThat(applicationUser.getEnabled()).isFalse();

        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("enable")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getFirstName()).isEqualTo("Fannie");
                    assertThat(response.getLastName()).isEqualTo("Hines");
                    assertThat(response.getEmail()).isEqualTo("fanniehines@email.com");
                    assertThat(response.getUserName()).isEqualTo("fanniehines");
                    assertThat(response.getVerified()).isEqualTo(true);
                    assertThat(response.getEnabled()).isEqualTo(true);
                });
    }

    @DisplayName("Enable user by id not found")
    @Test
    void enableUserByIdNotFound() {
        long userId = 1000L;
        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("enable")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User id not found")
                );
    }

    @DisplayName("Delete user by id")
    @Test
    void deleteUserById() {
        User applicationUser = userRepository.findByEmail("garylogan@email.com").get();
        assertThat(applicationUser).isNotNull();
        long userId = applicationUser.getId();

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        Optional<User> byEmail = userRepository.findByEmail("garylogan@email.com");
        assertThat(byEmail).isEmpty();
    }

    @DisplayName("Delete user by id not found")
    @Test
    void DeleteUserByIdNotFound() {
        long userId = 1000L;
        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User id not found")
                );
    }

    @DisplayName("Delete user by id user recently deleted return not found")
    @Test
    void deleteUserByIdRecentlyDeleted() {
        User applicationUser = userRepository.findByEmail("laurieadams@email.com").get();
        assertThat(applicationUser).isNotNull();
        long userId = applicationUser.getId();

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        Optional<User> byEmail = userRepository.findByEmail("laurieadams@email.com");
        assertThat(byEmail).isEmpty();

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User id not found")
                );
    }

    @DisplayName("Add authorities to user")
    @Test
    void addAuthorities() {
        User applicationUser = userRepository.findByEmail("lorettastanley@email.com").get();
        assertThat(applicationUser).isNotNull();
        long userId = applicationUser.getId();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getAuthorities()).hasSize(1);
                    assertThat(response.getAuthorities()).containsExactlyInAnyOrder("ROLE_USER");
                });

        AuthoritiesChange authoritiesChange = new AuthoritiesChange(singletonList("ROLE_ADMIN"));
        webTestClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getAuthorities()).hasSize(2);
                    assertThat(response.getAuthorities()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
                });
    }

    @DisplayName("Add same authorities that is already for the user do not add it again")
    @Test
    void addSameAuthorities() {
        User applicationUser = userRepository.findByEmail("natasharivera@email.com").get();
        assertThat(applicationUser).isNotNull();
        long userId = applicationUser.getId();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getAuthorities()).hasSize(1);
                    assertThat(response.getAuthorities()).containsExactlyInAnyOrder("ROLE_USER");
                });

        AuthoritiesChange authoritiesChange = new AuthoritiesChange(singletonList("ROLE_USER"));
        webTestClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getAuthorities()).hasSize(1);
                    assertThat(response.getAuthorities()).containsExactlyInAnyOrder("ROLE_USER");
                });
    }

    @DisplayName("Add authorities with invalid name do not add it")
    @Test
    void addAuthoritiesInvalidName() {
        User applicationUser = userRepository.findByEmail("natasharivera@email.com").get();
        assertThat(applicationUser).isNotNull();
        long userId = applicationUser.getId();

        AuthoritiesChange authoritiesChange = new AuthoritiesChange(singletonList("ROLE_ANOTHER"));
        webTestClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getAuthorities()).hasSize(1);
                    assertThat(response.getAuthorities()).containsExactlyInAnyOrder("ROLE_USER");
                });
    }

    @DisplayName("Add authorities user by id not found")
    @Test
    void addAuthoritiesUserNotFound() {
        long userId = 1000L;
        AuthoritiesChange authoritiesChange = new AuthoritiesChange(singletonList("ROLE_ANOTHER"));
        webTestClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User id not found")
                );
    }

    @DisplayName("Delete authority from user")
    @Test
    void deleteAuthority() {
        User applicationUser = userRepository.findByEmail("norawaters@email.com").get();
        assertThat(applicationUser).isNotNull();
        long userId = applicationUser.getId();

        AuthoritiesChange authoritiesChange = new AuthoritiesChange(singletonList("ROLE_ADMIN"));
        webTestClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getAuthorities()).hasSize(2);
                    assertThat(response.getAuthorities()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
                });

        authoritiesChange = new AuthoritiesChange(singletonList("ROLE_USER"));
        webTestClient
                .method(HttpMethod.DELETE)
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getAuthorities()).hasSize(1);
                    assertThat(response.getAuthorities()).containsExactlyInAnyOrder("ROLE_ADMIN");
                });
    }

    @DisplayName("Delete authority that does not exist for the user")
    @Test
    void deleteAuthorityNotThere() {
        User applicationUser = userRepository.findByEmail("pedrosullivan@email.com").get();
        assertThat(applicationUser).isNotNull();
        long userId = applicationUser.getId();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getAuthorities()).hasSize(1);
                    assertThat(response.getAuthorities()).containsExactlyInAnyOrder("ROLE_USER");
                });

        AuthoritiesChange authoritiesChange = new AuthoritiesChange(singletonList("ROLE_ADMIN"));
        webTestClient
                .method(HttpMethod.DELETE)
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getAuthorities()).hasSize(1);
                    assertThat(response.getAuthorities()).containsExactlyInAnyOrder("ROLE_USER");
                });
    }

    @DisplayName("Delete authority that is invalid for the user")
    @Test
    void deleteAuthorityInvalid() {
        User applicationUser = userRepository.findByEmail("phyllisterry@email.com").get();
        assertThat(applicationUser).isNotNull();
        long userId = applicationUser.getId();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getAuthorities()).hasSize(1);
                    assertThat(response.getAuthorities()).containsExactlyInAnyOrder("ROLE_USER");
                });

        AuthoritiesChange authoritiesChange = new AuthoritiesChange(singletonList("ROLE_ANOTHER"));
        webTestClient
                .method(HttpMethod.DELETE)
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getAuthorities()).hasSize(1);
                    assertThat(response.getAuthorities()).containsExactlyInAnyOrder("ROLE_USER");
                });
    }

    @DisplayName("Delete all authorities from user")
    @Test
    void deleteAllAuthorities() {
        long userId = getUserId("sabrinagarcia@email.com");

        AuthoritiesChange authoritiesChange = new AuthoritiesChange(singletonList("ROLE_ADMIN"));
        webTestClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getAuthorities()).hasSize(2);
                    assertThat(response.getAuthorities()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
                });

        authoritiesChange = new AuthoritiesChange(asList("ROLE_USER", "ROLE_ADMIN"));
        webTestClient
                .method(HttpMethod.DELETE)
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getAuthorities()).hasSize(0);
                });
    }

    @DisplayName("Delete authorities user by id not found")
    @Test
    void deleteAuthoritiesUserNotFound() {
        long userId = 1000L;
        AuthoritiesChange authoritiesChange = new AuthoritiesChange(singletonList("ROLE_ADMIN"));
        webTestClient
                .method(HttpMethod.DELETE)
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User id not found")
                );
    }

    private long getUserId(String email) {
        User applicationUser = userRepository.findByEmail(email).get();
        assertThat(applicationUser).isNotNull();
        return applicationUser.getId();
    }
}