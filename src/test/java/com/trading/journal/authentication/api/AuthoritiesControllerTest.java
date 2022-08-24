package com.trading.journal.authentication.api;

import com.trading.journal.authentication.PostgresTestContainerInitializer;
import com.trading.journal.authentication.WithCustomMockUser;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.AuthorityRepository;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
class AuthoritiesControllerTest {

    @Autowired
    AuthorityRepository authorityRepository;

    @Autowired
    UserAuthorityRepository userAuthorityRepository;

    @Autowired
    UserRepository userRepository;

    private static WebTestClient webTestClient;

    @BeforeAll
    public static void setUp(@Autowired WebApplicationContext applicationContext) {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @DisplayName("Get all authorities")
    @Test
    @WithCustomMockUser(username = "ADMIN", authorities = {"ROLE_ADMIN"})
    void getAll() {
        webTestClient
                .get()
                .uri("/admin/authorities")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Authority>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(3);
                    assertThat(response).extracting(Authority::getName).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "TENANCY_ADMIN");
                    assertThat(response).extracting(Authority::getCategory).containsExactlyInAnyOrder(AuthorityCategory.COMMON_USER, AuthorityCategory.ADMINISTRATOR, AuthorityCategory.ORGANISATION);
                });
    }

    @DisplayName("Get all authorities category")
    @Test
    @WithCustomMockUser(username = "ADMIN", authorities = {"ROLE_ADMIN"})
    void getAllCategories() {
        webTestClient
                .get()
                .uri("/admin/authorities/categories")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<AuthorityCategory>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(3);
                    assertThat(response).containsExactlyInAnyOrder(AuthorityCategory.COMMON_USER, AuthorityCategory.ADMINISTRATOR, AuthorityCategory.ORGANISATION);
                });
    }

    @DisplayName("Get authority by id")
    @Test
    @WithCustomMockUser(username = "ADMIN", authorities = {"ROLE_ADMIN"})
    void byId() {
        Optional<Authority> roleUser = authorityRepository.getByName("ROLE_USER");
        assertThat(roleUser).isNotEmpty();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/authorities/{id}")
                        .build(roleUser.get().getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Authority.class)
                .value(response -> {
                    assertThat(response.getCategory()).isEqualTo(AuthorityCategory.COMMON_USER);
                    assertThat(response.getName()).isEqualTo("ROLE_USER");
                });

        Optional<Authority> roleAdmin = authorityRepository.getByName("ROLE_ADMIN");
        assertThat(roleAdmin).isNotEmpty();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/authorities/{id}")
                        .build(roleAdmin.get().getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Authority.class)
                .value(response -> {
                    assertThat(response.getCategory()).isEqualTo(AuthorityCategory.ADMINISTRATOR);
                    assertThat(response.getName()).isEqualTo("ROLE_ADMIN");
                });

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/authorities/{id}")
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("Authority id not found")
                );
    }

    @DisplayName("Create a new authority")
    @Test
    @WithCustomMockUser(username = "ADMIN", authorities = {"ROLE_ADMIN"})
    void create() {
        webTestClient
                .post()
                .uri("/admin/authorities")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new Authority(AuthorityCategory.COMMON_USER, "ANOTHER_ROLE"))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists("Location")
                .expectBody(Authority.class)
                .value(response -> {
                    assertThat(response.getCategory()).isEqualTo(AuthorityCategory.COMMON_USER);
                    assertThat(response.getName()).isEqualTo("ANOTHER_ROLE");
                });

        webTestClient
                .post()
                .uri("/admin/authorities")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new Authority(AuthorityCategory.COMMON_USER, "ANOTHER_ROLE"))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT)
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("Authority name already exists")
                );

        Optional<Authority> another_role = authorityRepository.getByName("ANOTHER_ROLE");
        assertThat(another_role).isNotEmpty();
        authorityRepository.deleteById(another_role.get().getId());
    }

    @DisplayName("Update a authority")
    @Test
    @WithCustomMockUser(username = "ADMIN", authorities = {"ROLE_ADMIN"})
    void update() {
        Optional<Authority> roleUser = authorityRepository.getByName("ROLE_USER");
        assertThat(roleUser).isNotEmpty();

        webTestClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/authorities/{id}")
                        .build(roleUser.get().getId()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new Authority(AuthorityCategory.COMMON_USER, "ROLE_USER_UPDATED"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Authority.class)
                .value(response -> {
                    assertThat(response.getCategory()).isEqualTo(AuthorityCategory.COMMON_USER);
                    assertThat(response.getName()).isEqualTo("ROLE_USER_UPDATED");
                });

        List<Authority> authorities = authorityRepository.findAll();
        assertThat(authorities).extracting(Authority::getName).containsExactlyInAnyOrder("ROLE_USER_UPDATED", "ROLE_ADMIN", "TENANCY_ADMIN");

        webTestClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/authorities/{id}")
                        .build(roleUser.get().getId()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new Authority(AuthorityCategory.COMMON_USER, "ROLE_ADMIN"))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT)
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("Authority name already exists")
                );

        webTestClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/authorities/{id}")
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new Authority(AuthorityCategory.COMMON_USER, "ROLE_ADMIN"))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NOT_FOUND)
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("Authority id not found")
                );

        authorityRepository.save(new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"));
    }

    @DisplayName("Delete a authority")
    @Test
    @WithCustomMockUser(username = "ADMIN", authorities = {"ROLE_ADMIN"})
    void delete() {
        User user = User.builder()
                .userName("johnwick")
                .password("dad231#$#4")
                .firstName("John")
                .lastName("Wick")
                .email("johnwick@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);
        Authority anotherRole = authorityRepository.save(new Authority(AuthorityCategory.COMMON_USER, "ANOTHER_ROLE"));
        UserAuthority anotherRoleUserAuthority = userAuthorityRepository.save(
                UserAuthority.builder()
                        .authority(anotherRole)
                        .user(user)
                        .build()
        );

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/authorities/{id}")
                        .build(anotherRole.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("Authority is used by one or more user")
                );

        userAuthorityRepository.delete(anotherRoleUserAuthority);

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/authorities/{id}")
                        .build(anotherRole.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/authorities/{id}")
                        .build(anotherRole.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NOT_FOUND)
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("Authority id not found")
                );
    }
}