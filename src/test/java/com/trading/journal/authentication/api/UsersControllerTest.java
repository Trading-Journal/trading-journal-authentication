package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.WithCustomMockUser;
import com.trading.journal.authentication.authority.AuthoritiesHelper;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.service.AuthorityService;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
@WithCustomMockUser(authorities = {"TENANCY_ADMIN"})
class UsersControllerTest {

    public static final String USER_PATH = "/admin/users/{userId}";
    public static final String USERS_AUTHORITIES_PATH = "/admin/users/{userId}/authorities";
    public static final String USERS_ENABLE_PATH = "/admin/users/{userId}/enable";
    public static final String USERS_DISABLE_PATH = "/admin/users/{userId}/disable";

    @Autowired
    UserRepository userRepository;

    private static WebTestClient webTestClient;

    private static Tenancy tenancy;

    @BeforeAll
    public static void setUp(
            @Autowired WebApplicationContext applicationContext,
            @Autowired TenancyRepository tenancyRepository,
            @Autowired UserRepository userRepository,
            @Autowired UserAuthorityRepository userAuthorityRepository,
            @Autowired AuthorityService authorityService) {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext).build();
        tenancy = tenancyRepository.save(Tenancy.builder().name("tenancy").build());
        Stream<String> users = Stream.of(
                "Andy Johnson", "Angel Duncan", "Angelo Wells", "Arthur Lawrence", "Bernard Myers", "Beth Guzman", "Blake Coleman", "Brian Mann", "Cameron Fleming", "Carlton Santos",
                "Carrie Tate", "Catherine Jones", "Cecil Perkins", "Colin Ward", "Conrad Hernandez", "Dolores Williamson", "Doris Parker", "Earl Norris", "Eddie Massey", "Elena Boyd",
                "Elisa Vargas", "Erma Black", "Ernestine Steele", "Ernesto Kim", "Fannie Hines", "Gabriel Dixon", "Gary Logan", "Gerard Webb", "Ida Garza", "Isaac James",
                "Jerome Pratt", "Joel Dunn", "Julie Carson", "Kathy Oliver", "Katrina Hawkins", "Larry Robbins", "Laurie Adams", "Loretta Stanley", "Luke Tyler", "Melinda Fields",
                "Natasha Rivera", "Nora Waters", "Pedro Sullivan", "Phyllis Terry", "Rochelle Graves", "Sabrina Garcia", "Sadie Davis", "Vera Lamb", "Verna Wilkins", "Victoria Luna"
        );
        Authority authority = authorityService.getByName(AuthoritiesHelper.ROLE_USER.getLabel()).get();

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
                }).map(userRepository::save)
                .map(applicationUser -> new UserAuthority(applicationUser, authority))
                .forEach(userAuthorityRepository::save);
    }

    @AfterAll
    public static void shotDown(@Autowired UserRepository userRepository, @Autowired TenancyRepository tenancyRepository) {
        userRepository.deleteAll();
        tenancyRepository.deleteAll();
    }

    @DisplayName("Get user by id")
    @Test
    void getUserById() {
        long userId = getUserId("ermablack@email.com");

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User not found")
                );
    }

    @DisplayName("Disable user by id")
    @Test
    void disableUserById() {
        long userId = getUserId("ernestokim@email.com");

        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(USERS_DISABLE_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USERS_DISABLE_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User not found")
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
                        .path(USERS_ENABLE_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USERS_ENABLE_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User not found")
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
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User not found")
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
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk();

        Optional<User> byEmail = userRepository.findByEmail("laurieadams@email.com");
        assertThat(byEmail).isEmpty();

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User not found")
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
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USERS_AUTHORITIES_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USERS_AUTHORITIES_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USERS_AUTHORITIES_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USERS_AUTHORITIES_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User not found")
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
                        .path(USERS_AUTHORITIES_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USERS_AUTHORITIES_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USERS_AUTHORITIES_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USERS_AUTHORITIES_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USERS_AUTHORITIES_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USERS_AUTHORITIES_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .header("tenancy", tenancy.getId().toString())
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
                        .path(USERS_AUTHORITIES_PATH)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .header("tenancy", tenancy.getId().toString())
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User not found")
                );
    }

    private long getUserId(String email) {
        User applicationUser = userRepository.findByEmail(email).get();
        assertThat(applicationUser).isNotNull();
        return applicationUser.getId();
    }
}
