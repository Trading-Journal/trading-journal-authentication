package com.trading.journal.authentication.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.authentication.AuthenticationService;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.authority.UserAuthorityRepository;
import com.trading.journal.authentication.jwt.PrivateKeyProvider;
import com.trading.journal.authentication.jwt.data.JwtProperties;
import com.trading.journal.authentication.jwt.helper.DateHelper;
import com.trading.journal.authentication.jwt.helper.JwtConstants;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.user.ApplicationUserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
@TestPropertySource(properties = {"journal.authentication.authority.type=STATIC"})
public class AuthenticationControllerTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApplicationUserService applicationUserService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PrivateKeyProvider privateKeyProvider;

    @Autowired
    private JwtProperties properties;

    @Autowired
    private UserAuthorityRepository userAuthorityRepository;

    @Autowired
    ApplicationUserRepository applicationUserRepository;

    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
        applicationUserRepository.deleteAll().block();
    }

    @Test
    @DisplayName("When signUp as new user return success and the UserAuthority entity has AuthorityId because the authorities are static")
    void signUp() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName2",
                "mail2@mail.com",
                "dad231#$#4",
                "dad231#$#4");

        webTestClient
                .post()
                .uri("/authentication/signup")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userRegistration)
                .exchange()
                .expectStatus()
                .isOk();

        List<UserAuthority> userAuthorities = userAuthorityRepository.findAll().collectList().block();
        assert userAuthorities != null;
        userAuthorities.forEach(userAuthority -> assertThat(userAuthority.getAuthorityId()).isNull());
    }

    @Test
    @DisplayName("When signIn user return success and token")
    void signIn() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "dad231#$#4",
                "dad231#$#4");

        applicationUserService.createNewUser(userRegistration).block();

        Login login = new Login("mail@mail.com", "dad231#$#4");

        webTestClient
                .post()
                .uri("/authentication/signin")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(login)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(LoginResponse.class)
                .value(response -> {
                    assertThat(response.accessToken()).isNotBlank();
                    assertThat(response.refreshToken()).isNotBlank();
                });
    }

    @Test
    @DisplayName("When signIn user that does not exist, return 401")
    void signInFails() {
        Login login = new Login("mail3@mail.com", "dad231#$#4");

        webTestClient
                .post()
                .uri("/authentication/signin")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(login)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("error")).isEqualTo("User mail3@mail.com does not exist"));
    }

    @Test
    @DisplayName("When signIn with wrong password, return 401")
    void signInFailsPassword() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName4",
                "mail4@mail.com",
                "dad231#$#4",
                "dad231#$#4");

        applicationUserService.createNewUser(userRegistration).block();

        Login login = new Login("mail4@mail.com", "wrong_password");

        webTestClient
                .post()
                .uri("/authentication/signin")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(login)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("error")).isEqualTo("Invalid Credentials"));
    }

    @Test
    @DisplayName("When refreshing token, return success and new token")
    void refreshToken() {
        UserRegistration user = new UserRegistration(
                "John",
                "Travolta",
                "johntravolta",
                "johntravolta@mail.com",
                "dad231#$#4",
                "dad231#$#4");

        applicationUserService.createNewUser(user).block();

        Login login = new Login(user.email(), user.password());

        LoginResponse loginResponse = authenticationService.signIn(login).block();

        assert loginResponse != null;
        webTestClient
                .post()
                .uri("/authentication/refresh-token")
                .header("refresh-token", loginResponse.refreshToken())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(LoginResponse.class)
                .value(response -> {
                    assertThat(response.accessToken()).isNotBlank();
                    assertThat(response.refreshToken()).isNotBlank();
                });
    }

    @Test
    @DisplayName("When refreshing token with access token, return unauthorized exception")
    void refreshTokenUnauthorized() {
        UserRegistration user = new UserRegistration(
                "allan",
                "weber",
                "allanweber",
                "allanweber@mail.com",
                "dad231#$#4",
                "dad231#$#4");

        applicationUserService.createNewUser(user).block();

        Login login = new Login(user.email(), user.password());

        LoginResponse loginResponse = authenticationService.signIn(login).block();

        assert loginResponse != null;
        webTestClient
                .post()
                .uri("/authentication/refresh-token")
                .header("refresh-token", loginResponse.accessToken())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("error"))
                        .isEqualTo("Refresh token is invalid or is not a refresh token"));
    }

    @Test
    @DisplayName("When refreshing token with expired token, return unauthorized exception")
    void refreshTokenExpired() throws IOException {
        Key privateKey = privateKeyProvider.provide(this.properties.getPrivateKey());

        Date expiration = Date.from(LocalDateTime.now().minusSeconds(1L)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        Date issuedAt = DateHelper.getUTCDatetimeAsDate();
        String refreshToken = Jwts.builder()
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer("trade-journal")
                .setAudience("https://tradejournal.biz")
                .setSubject("allan")
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .claim(JwtConstants.SCOPES, Collections.singletonList(JwtConstants.REFRESH_TOKEN))
                .compact();

        webTestClient
                .post()
                .uri("/authentication/refresh-token")
                .header("refresh-token", refreshToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .value(response -> assertThat(response.get("error"))
                        .isEqualTo("Refresh token is expired"));
    }
}
