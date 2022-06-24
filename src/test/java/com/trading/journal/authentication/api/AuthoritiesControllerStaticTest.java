package com.trading.journal.authentication.api;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.authentication.service.AuthenticationService;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
@TestPropertySource(properties = {"journal.authentication.authority.type=STATIC"})
class AuthoritiesControllerStaticTest {

    private static String token;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    public static void setUp(
            @Autowired ApplicationUserRepository applicationUserRepository,
            @Autowired PasswordEncoder encoder,
            @Autowired AuthenticationService authenticationService,
            @Autowired UserAuthorityService userAuthorityService
    ) {
        ApplicationUser applicationUser = applicationUserRepository.save(new ApplicationUser(
                null,
                "johnwick2",
                encoder.encode("dad231#$#4"),
                "John",
                "Wick",
                "johnwick2@mail.com",
                true,
                true,
                emptyList(),
                LocalDateTime.now()));
        userAuthorityService.saveAdminUserAuthorities(applicationUser);

        Login login = new Login("johnwick2@mail.com", "dad231#$#4");
        LoginResponse loginResponse = authenticationService.signIn(login);
        assertThat(loginResponse).isNotNull();
        token = loginResponse.accessToken();
    }

    @DisplayName("Get all authorities")
    @Test
    void getAll() {
        webTestClient
                .get()
                .uri("/admin/authorities")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Authority>>() {
                })
                .value(response -> {
                    assertThat(response).hasSize(2);
                    assertThat(response).extracting(Authority::getName).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
                    assertThat(response).extracting(Authority::getCategory).containsExactlyInAnyOrder(AuthorityCategory.COMMON_USER, AuthorityCategory.ADMINISTRATOR);
                    assertThat(response).extracting(Authority::getId).containsExactlyInAnyOrder(null, null);
                });
    }
}