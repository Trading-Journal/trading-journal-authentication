package com.trading.journal.authentication.configuration;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.authentication.service.AuthenticationService;
import com.trading.journal.authentication.email.service.EmailSender;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.user.service.AdminUserService;
import com.trading.journal.authentication.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
public class SecurityConfigurationTest {

    @Autowired
    UserService userService;

    @Autowired
    AdminUserService adminUserService;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    EmailSender emailSender;

    @BeforeEach
    public void setUp() {
        doNothing().when(emailSender).send(any());
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Access public paths anonymously")
    void anonymously() {
        webTestClient
                .get()
                .uri("/swagger-ui/index.html")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @DisplayName("Access protected path anonymously fails")
    void anonymouslyFails() {
        webTestClient
                .get()
                .uri("/me")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @ParameterizedTest
    @MethodSource("invalidTokens")
    @DisplayName("Access protected path with invalid token fails")
    void invalidToken(String token) {
        webTestClient
                .get()
                .uri("/me")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @DisplayName("Access users admin path with common user token fails")
    @Test
    void invalidAdminAccess() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "John",
                "Wick",
                "johnwick",
                "johnwick@mail.com",
                "dad231#$#4",
                "dad231#$#4");
        userService.createNewUser(userRegistration, null);
        Login login = new Login(userRegistration.getEmail(), userRegistration.getPassword());
        LoginResponse loginResponse = authenticationService.signIn(login);
        assertThat(loginResponse).isNotNull();

        webTestClient
                .get()
                .uri("/admin/users")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + loginResponse.accessToken())
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @DisplayName("Access users admin path with Admin user is granted")
    @Test
    void adminAccess() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "John",
                "Wick",
                "johnwick",
                "johnwick@mail.com",
                "dad231#$#4",
                "dad231#$#4");
        adminUserService.createAdmin(userRegistration);

        User applicationUser = userRepository.findByEmail("johnwick@mail.com").get();
        applicationUser.enable();
        applicationUser.verify();
        applicationUser.changePassword(encoder.encode("dad231#$#4"));
        userRepository.save(applicationUser);

        Login login = new Login(userRegistration.getEmail(), userRegistration.getPassword());
        LoginResponse loginResponse = authenticationService.signIn(login);
        assertThat(loginResponse).isNotNull();

        webTestClient
                .get()
                .uri("/admin/users")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + loginResponse.accessToken())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @DisplayName("Access authorities admin path with common user token fails")
    @Test
    void invalidAdminAccessAuthorities() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "John",
                "Wick",
                "johnwick",
                "johnwick@mail.com",
                "dad231#$#4",
                "dad231#$#4");
        userService.createNewUser(userRegistration, null);
        Login login = new Login(userRegistration.getEmail(), userRegistration.getPassword());
        LoginResponse loginResponse = authenticationService.signIn(login);
        assertThat(loginResponse).isNotNull();

        webTestClient
                .get()
                .uri("/admin/authorities")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + loginResponse.accessToken())
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @DisplayName("Access authorities admin path with Admin user is granted")
    @Test
    void adminAccessAuthorities() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "John",
                "Wick",
                "johnwick",
                "johnwick@mail.com",
                "dad231#$#4",
                "dad231#$#4");
        adminUserService.createAdmin(userRegistration);

        User applicationUser = userRepository.findByEmail("johnwick@mail.com").get();
        applicationUser.enable();
        applicationUser.verify();
        applicationUser.changePassword(encoder.encode("dad231#$#4"));
        userRepository.save(applicationUser);

        Login login = new Login(userRegistration.getEmail(), userRegistration.getPassword());
        LoginResponse loginResponse = authenticationService.signIn(login);
        assertThat(loginResponse).isNotNull();

        webTestClient
                .get()
                .uri("/admin/authorities")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + loginResponse.accessToken())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @DisplayName("Access tenancies path with Admin user is granted")
    @Test
    void adminAccessTenancy() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "John",
                "Wick",
                "johnwick",
                "johnwick@mail.com",
                "dad231#$#4",
                "dad231#$#4");
        adminUserService.createAdmin(userRegistration);

        User applicationUser = userRepository.findByEmail("johnwick@mail.com").get();
        applicationUser.enable();
        applicationUser.verify();
        applicationUser.changePassword(encoder.encode("dad231#$#4"));
        userRepository.save(applicationUser);

        Login login = new Login(userRegistration.getEmail(), userRegistration.getPassword());
        LoginResponse loginResponse = authenticationService.signIn(login);
        assertThat(loginResponse).isNotNull();

        webTestClient
                .get()
                .uri("/admin/tenancies")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + loginResponse.accessToken())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @DisplayName("Access tenancies path with common user token fails")
    @Test
    void invalidAccessTenancy() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "John",
                "Wick",
                "johnwick",
                "johnwick@mail.com",
                "dad231#$#4",
                "dad231#$#4");
        userService.createNewUser(userRegistration, null);
        Login login = new Login(userRegistration.getEmail(), userRegistration.getPassword());
        LoginResponse loginResponse = authenticationService.signIn(login);
        assertThat(loginResponse).isNotNull();

        webTestClient
                .get()
                .uri("/admin/tenancies")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + loginResponse.accessToken())
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    private static Stream<String> invalidTokens() {
        return Stream.of(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ0cmFkZS1qb3VybmFsIiwiYXVkIjoiaHR0cHM6Ly90cmFkZWpvdXJuYWwuYml6Iiwic3ViIjoiYWxsYW53ZWJlciIsImlhdCI6MTY1Mjg2NjkyNCwiZXhwIjoxNjUyODc0MTI5LCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwidGVuYW5jeSI6ImFsbGFud2ViZXIifQ.DbSUHKMuPPUpUYYuRHFsdHTsTKgMPHU1AYGC5eQPrPgZPFN0j71JOlhLTWnpqiod3Hq_Y4kmExL-MS4jUlgVRsWkbigjNgdhmy2XaBhbGJpJkC8-v1U-tlh8bBTT7zHrfLsR44FBlNVUqDcCoAAIMshMth2mTLkgHufdVS4IxuLOrWq9mwX6YuZZBYRSxdUEK0S8ut5Sk6pJWtViB-eMXByVPqUhBOj6rfypgBSFqrOq4hNZ7rbm9T5AwIctNvzAqGtQ0j9dj5KeJdZuEoaI_Lrcdi8PHTrPx15hh7XMivUgqBk4kQcbXDqTUNOP2-sZV1SPDmJhVzvV9WV0KNDMOw",
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ0cmFkZS1qb3VybmFsIiwiYXVkIjoiaHR0cHM6Ly90cmFkZWpvdXJuYWwuYml6Iiwic3ViIjoiYWxsYW53ZWJlciIsImlhdCI6MTY1Mjg2NzEwOCwiZXhwIjoxNjUyODc0MzEzLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwidGVuYW5jeSI6ImFsbGFud2ViZXIifQ.goAON4fRydNyx7zV5qU2NB00V3HtqM_HZ3Z6-jGM--rkwCTwhpkgWCi3STf-fF8uRu2xZC_vY-TTWQYyQO6dKivWx5CNNzPsLdEMPSiPutnf52lDh0cQ0K5W7RFKsuT_XG26UvQTR0yo8m-3eucaPk6zeas3yCQCKgQxmXrd58e8F8Ai1TppnL-kYGTar4Z6Xi5mBxVhOkIarnPMLewcQNHsz9za_F4IPzDK5MoSfIOWrEv-NHSSq_75UG3_8SyWsD8Zv6r9qw8Kpun-dOSew4fqtpE3qZkqfQr9j6R-Oc8oFySzhcFBVVj6-OiLIEc_vUHVy6JZM467vJh3XjL_3g");
    }
}
