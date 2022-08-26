package com.trading.journal.authentication.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.allanweber.jwttoken.service.JwtResolveToken;
import com.allanweber.jwttoken.service.JwtTokenReader;
import com.trading.journal.authentication.PostgresTestContainerInitializer;
import com.trading.journal.authentication.WithCustomMockUser;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.service.TenancyService;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.UserManagementRepository;
import com.trading.journal.authentication.user.service.UserService;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
class OrganisationUsersControllerMultiTenancyTest {

    public static final String PATH = "/organisation/users";

    public static final String PATH_BY_ID = "/organisation/users/{id}";

    @MockBean
    JwtTokenReader tokenReader;

    @MockBean
    JwtResolveToken resolveToken;

    @MockBean
    UserManagementRepository userManagementRepository;

    @MockBean
    TenancyService tenancyService;

    @MockBean
    VerificationService verificationService;

    @MockBean
    UserAuthorityService userAuthorityService;

    @MockBean
    UserService userService;

    private static WebTestClient webTestClient;

    @BeforeAll
    public static void setUp(@Autowired WebApplicationContext applicationContext) {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @BeforeEach
    public void mockResolveToken() {
        when(resolveToken.resolve(any())).thenReturn("token");
    }

    @DisplayName("Page organisation users tenancy Tenancy10")
    @Test
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 10L, tenancyName = "tenancy10")
    void pageTenancyCalled10() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 10L, "tenancy10", singletonList("TENANCY_ADMIN")));

        when(userManagementRepository.findAll(any(), any(PageRequest.class))).thenReturn(new PageImpl<User>(
                asList(User.builder()
                                .userName("userName1")
                                .email("userName1@mail.com")
                                .password(UUID.randomUUID().toString())
                                .firstName("user 1")
                                .lastName("tenancy 10")
                                .enabled(true)
                                .verified(true)
                                .createdAt(LocalDateTime.now())
                                .build(),
                        User.builder()
                                .userName("userName2")
                                .email("userName2@mail.com")
                                .password(UUID.randomUUID().toString())
                                .firstName("user 2")
                                .lastName("tenancy 10")
                                .enabled(true)
                                .verified(true)
                                .createdAt(LocalDateTime.now())
                                .build(),
                        User.builder()
                                .userName("userName3")
                                .email("userName3@mail.com")
                                .password(UUID.randomUUID().toString())
                                .firstName("user 3")
                                .lastName("tenancy 10")
                                .enabled(true)
                                .verified(true)
                                .createdAt(LocalDateTime.now())
                                .build()
                ),
                PageRequest.of(0, 1),
                3
        ));

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("filter", "user ")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserInfo>>() {
                })
                .value(response -> {
                    assertThat(response.items()).hasSize(3);
                    assertThat(response.currentPage()).isEqualTo(0);
                    assertThat(response.items()).extracting(userInfo -> userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()))
                            .containsExactly("user 1 tenancy 10", "user 2 tenancy 10", "user 3 tenancy 10");
                });
    }

    @DisplayName("Get user by id")
    @Test
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void getUserById() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        when(userManagementRepository.findByTenancyIdAndId(20L, 100L)).thenReturn(
                Optional.of(User.builder()
                        .userName("userName1")
                        .email("userName1@mail.com")
                        .password(UUID.randomUUID().toString())
                        .enabled(true)
                        .verified(true)
                        .createdAt(LocalDateTime.now())
                        .build())
        );

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getEmail()).isEqualTo("userName1@mail.com");
                    assertThat(response.getVerified()).isEqualTo(true);
                    assertThat(response.getEnabled()).isEqualTo(true);
                });
    }

    @DisplayName("Get user by id not found")
    @Test
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void getUserByIdNotFound() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        long userId = 1000L;
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
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
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void disableUserById() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        when(userManagementRepository.findByTenancyIdAndId(20L, 100L)).thenReturn(
                Optional.of(User.builder()
                        .userName("userName1")
                        .email("userName1@mail.com")
                        .password(UUID.randomUUID().toString())
                        .enabled(true)
                        .verified(true)
                        .createdAt(LocalDateTime.now())
                        .build())
        );

        when(userManagementRepository.save(argThat(user -> user.getEnabled().equals(false)))).thenReturn(User.builder()
                .userName("userName1")
                .email("userName1@mail.com")
                .password(UUID.randomUUID().toString())
                .enabled(false)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .build());

        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("disable")
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getVerified()).isEqualTo(true);
                    assertThat(response.getEnabled()).isEqualTo(false);
                });
    }

    @DisplayName("Disable user by id not found")
    @Test
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void disableUserByIdNotFound() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("disable")
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
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
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void enableUserById() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        when(userManagementRepository.findByTenancyIdAndId(20L, 100L)).thenReturn(
                Optional.of(User.builder()
                        .userName("userName1")
                        .email("userName1@mail.com")
                        .password(UUID.randomUUID().toString())
                        .enabled(false)
                        .verified(true)
                        .createdAt(LocalDateTime.now())
                        .build())
        );

        when(userManagementRepository.save(argThat(user -> user.getEnabled().equals(true)))).thenReturn(User.builder()
                .userName("userName1")
                .email("userName1@mail.com")
                .password(UUID.randomUUID().toString())
                .enabled(false)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .build());

        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("enable")
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getVerified()).isEqualTo(true);
                    assertThat(response.getEnabled()).isEqualTo(true);
                });
    }

    @DisplayName("Enable user by id not found")
    @Test
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void enableUserByIdNotFound() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("enable")
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
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
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void deleteUserById() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        when(userManagementRepository.findByTenancyIdAndId(20L, 100L)).thenReturn(
                Optional.of(User.builder()
                        .userName("userName1")
                        .email("userName1@mail.com")
                        .password(UUID.randomUUID().toString())
                        .enabled(false)
                        .verified(true)
                        .createdAt(LocalDateTime.now())
                        .build())
        );

        when(tenancyService.lowerUsage(20L)).thenReturn(Tenancy.builder().build());

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @DisplayName("Delete user by id not found")
    @Test
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void DeleteUserByIdNotFound() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
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
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void addAuthorities() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        User user = User.builder()
                .userName("userName1")
                .email("userName1@mail.com")
                .password(UUID.randomUUID().toString())
                .enabled(false)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .build();
        when(userManagementRepository.findByTenancyIdAndId(20L, 100L)).thenReturn(Optional.of(user));

        AuthoritiesChange authoritiesChange = new AuthoritiesChange(singletonList("ROLE_ADMIN"));
        when(userAuthorityService.addAuthorities(user, authoritiesChange)).thenReturn(emptyList());

        webTestClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @DisplayName("Add authorities user by id not found")
    @Test
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void addAuthoritiesUserNotFound() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        AuthoritiesChange authoritiesChange = new AuthoritiesChange(singletonList("ROLE_ADMIN"));

        webTestClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
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
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void deleteAuthority() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        User user = User.builder()
                .userName("userName1")
                .email("userName1@mail.com")
                .password(UUID.randomUUID().toString())
                .enabled(false)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .build();
        when(userManagementRepository.findByTenancyIdAndId(20L, 100L)).thenReturn(Optional.of(user));

        AuthoritiesChange authoritiesChange = new AuthoritiesChange(singletonList("ROLE_ADMIN"));
        when(userAuthorityService.deleteAuthorities(user, authoritiesChange)).thenReturn(emptyList());

        authoritiesChange = new AuthoritiesChange(singletonList("ROLE_USER"));
        webTestClient
                .method(HttpMethod.DELETE)
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .exchange()
                .expectStatus()
                .isOk();
    }


    @DisplayName("Delete authorities user by id not found")
    @Test
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void deleteAuthoritiesUserNotFound() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        AuthoritiesChange authoritiesChange = new AuthoritiesChange(singletonList("ROLE_ADMIN"));
        webTestClient
                .method(HttpMethod.DELETE)
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_BY_ID)
                        .pathSegment("authorities")
                        .build(100L))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(authoritiesChange)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response ->
                        assertThat(response.get("error")).isEqualTo("User not found")
                );
    }

    @DisplayName("Create a new user")
    @Test
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void createUser() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        Tenancy tenancy = Tenancy.builder().userLimit(10).userUsage(1).build();
        when(tenancyService.getById(20L)).thenReturn(tenancy);

        UserRegistration userRegistration = new UserRegistration(
                null,
                "firstName",
                "lastName",
                "UserName5",
                "mail@mail.com",
                "dad231#$#4",
                "dad231#$#4");

        User user = User.builder()
                .id(1L)
                .userName("UserName5")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .build();
        when(userService.createNewUser(any(), eq(tenancy))).thenReturn(user);

        doNothing().when(verificationService).send(VerificationType.NEW_ORGANISATION_USER, user);

        when(tenancyService.increaseUsage(20L)).thenReturn(tenancy);

        webTestClient
                .post()
                .uri(PATH)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userRegistration)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getUserName()).isEqualTo(user.getUserName());
                    assertThat(response.getFirstName()).isEqualTo(user.getFirstName());
                    assertThat(response.getLastName()).isEqualTo(user.getLastName());
                    assertThat(response.getEmail()).isEqualTo(user.getEmail());
                });
    }

    @DisplayName("Create a new user without available user limit return exception")
    @Test
    @WithCustomMockUser(authorities = {"TENANCY_ADMIN"}, tenancyId = 20L, tenancyName = "tenancy20")
    void createUserNoLimit() {
        when(tokenReader.getAccessTokenInfo(anyString()))
                .thenReturn(new AccessTokenInfo("user", 20L, "tenancy10", singletonList("TENANCY_ADMIN")));

        Tenancy tenancy = Tenancy.builder().userLimit(10).userUsage(10).build();
        when(tenancyService.getById(20L)).thenReturn(tenancy);

        UserRegistration userRegistration = new UserRegistration(
                null,
                "firstName",
                "lastName",
                "UserName5",
                "mail@mail.com",
                "dad231#$#4",
                "dad231#$#4");

        webTestClient
                .post()
                .uri(PATH)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userRegistration)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("error")).isEqualTo("Tenancy has reach its user limit"));

        verify(userService, never()).createNewUser(any(), any());
        verify(verificationService, never()).send(any(), any());
        verify(tenancyService, never()).increaseUsage(anyLong());
    }
}