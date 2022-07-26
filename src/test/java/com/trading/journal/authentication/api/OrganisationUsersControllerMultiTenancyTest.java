package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.authentication.service.AuthenticationService;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserInfo;
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
class OrganisationUsersControllerMultiTenancyTest {

    public static final String PATH = "/organisation/users";

    public static final String PATH_BY_ID = "/organisation/users/{id}";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    UserRepository userRepository;
    @Autowired
    TenancyRepository tenancyRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    UserAuthorityService userAuthorityService;

    @BeforeAll
    public static void setUp(@Autowired UserRepository userRepository, @Autowired TenancyRepository tenancyRepository) {
        userRepository.deleteAll();
        tenancyRepository.deleteAll();
    }

    @DisplayName("Page organisation users for multiple tenancies")
    @Test
    void page() {
        Tenancy tenancy = createTenancy("tenancy 1");
        String token = createOrgAdminUser(tenancy, "username1");

        createSampleUser(tenancy, "user 1 tenancy 1");
        createSampleUser(tenancy, "user 2 tenancy 1");
        createSampleUser(tenancy, "user 2 tenancy 1");

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("filter", "user ")
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
                    assertThat(response.items()).extracting(UserInfo::getFirstName)
                            .containsExactly("user 1 tenancy 1", "user 2 tenancy 1", "user 2 tenancy 1");
                });

        tenancy = createTenancy("tenancy 2");
        token = createOrgAdminUser(tenancy, "username2");

        createSampleUser(tenancy, "user 1 tenancy 2");
        createSampleUser(tenancy, "user 2 tenancy 2");
        createSampleUser(tenancy, "user 2 tenancy 2");

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("filter", "user ")
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
                    assertThat(response.items()).extracting(UserInfo::getFirstName)
                            .containsExactly("user 1 tenancy 2", "user 2 tenancy 2", "user 2 tenancy 2");
                });
    }

    private Tenancy createTenancy(String name) {
        return tenancyRepository.save(Tenancy.builder().name(name).build());
    }

    private String createOrgAdminUser(Tenancy tenancy, String username) {
        User user = User.builder()
                .userName(username)
                .password(encoder.encode("dad231#$#4"))
                .firstName("John")
                .lastName("Wick")
                .email(username + "@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .tenancy(tenancy)
                .build();
        User applicationUser = userRepository.save(user);
        userAuthorityService.saveCommonUserAuthorities(applicationUser);
        userAuthorityService.saveOrganisationAdminUserAuthorities(applicationUser);

        Login login = new Login(username + "@mail.com", "dad231#$#4");
        LoginResponse loginResponse = authenticationService.signIn(login);
        assertThat(loginResponse).isNotNull();
        return loginResponse.accessToken();
    }

    private void createSampleUser(Tenancy tenancy, String name) {
        User user = User.builder()
                .userName(name)
                .password("dad231#$#4")
                .firstName(name)
                .lastName("Surname")
                .email(name + "@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .tenancy(tenancy)
                .build();
        userRepository.save(user);
    }
}