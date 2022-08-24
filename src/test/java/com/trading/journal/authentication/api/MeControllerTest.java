package com.trading.journal.authentication.api;

import com.trading.journal.authentication.PostgresTestContainerInitializer;
import com.trading.journal.authentication.WithCustomMockUser;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.jwt.service.JwtResolveToken;
import com.trading.journal.authentication.jwt.service.JwtTokenReader;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.service.TenancyService;
import com.trading.journal.authentication.user.*;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import com.trading.journal.authentication.verification.service.VerificationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
public class MeControllerTest {

    @MockBean
    JwtTokenReader tokenReader;

    @MockBean
    JwtResolveToken resolveToken;

    @MockBean
    UserRepository userRepository;

    @MockBean
    UserManagementRepository userManagementRepository;

    @MockBean
    VerificationEmailService verificationEmailService;

    @MockBean
    TenancyService tenancyService;

    @Autowired
    VerificationService verificationService;

    private static WebTestClient webTestClient;

    @BeforeAll
    public static void setUp(@Autowired WebApplicationContext applicationContext) {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @DisplayName("When logged user hit Me endpoint, return its information")
    @ParameterizedTest
    @MethodSource("feedUsers")
    @WithCustomMockUser
    void meEndpoint(UserRegistration user) {
        when(resolveToken.resolve(any())).thenReturn("token");
        when(tokenReader.getAccessTokenInfo(anyString())).thenReturn(new AccessTokenInfo("user", 1L, "tenancy", singletonList("ROLE_USER")));

        when(userManagementRepository.findByTenancyIdAndEmail(1L, "user")).thenReturn(
                Optional.of(User.builder()
                        .userName(user.getUserName())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .authorities(singletonList(UserAuthority.builder().authority(Authority.builder().name("ROLE_USER").build()).build()))
                        .build())
        );

        webTestClient
                .get()
                .uri("/me")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getUserName()).isEqualTo(user.getUserName());
                    assertThat(response.getFirstName()).isEqualTo(user.getFirstName());
                    assertThat(response.getLastName()).isEqualTo(user.getLastName());
                    assertThat(response.getEmail()).isEqualTo(user.getEmail());
                    assertThat(response.getAuthorities()).containsExactly("ROLE_USER");
                });
    }

    @DisplayName("Delete me")
    @Test
    @WithCustomMockUser
    void deleteMe() {
        String email = "mail@mail.com";
        long tenancyId = 1L;

        when(resolveToken.resolve(any())).thenReturn("token");
        when(tokenReader.getAccessTokenInfo(anyString())).thenReturn(new AccessTokenInfo(email, tenancyId, "tenancy", singletonList("ROLE_USER")));
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        User user = User.builder()
                .id(1L)
                .userName("userName")
                .firstName("firstName")
                .lastName("lastName")
                .email(email)
                .authorities(singletonList(UserAuthority.builder().authority(Authority.builder().name("ROLE_USER").build()).build()))
                .build();
        when(userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)).thenReturn(Optional.of(user));

        webTestClient
                .post()
                .uri("/me/delete")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        List<Verification> verifications = verificationService.getByEmail(email);
        assertThat(verifications).hasSize(1);
        String hash = verifications.get(0).getHash();

        when(tokenReader.isTokenValid(hash)).thenReturn(true);

        AccessTokenInfo hashInfo = new AccessTokenInfo(email, tenancyId, "tenancy", singletonList("TEMPORARY_TOKEN"));
        when(tokenReader.getTokenInfo(hash)).thenReturn(hashInfo);

        when(userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)).thenReturn(Optional.of(user));
        when(userManagementRepository.findByTenancyIdAndId(tenancyId, 1L)).thenReturn(Optional.of(user));
        doNothing().when(userManagementRepository).delete(user);
        when(tenancyService.lowerUsage(tenancyId)).thenReturn(Tenancy.builder().build());
        when(userRepository.existsByTenancyId(tenancyId)).thenReturn(false);
        doNothing().when(tenancyService).delete(tenancyId);

        webTestClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/delete")
                        .queryParam("hash", hash)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        verifications = verificationService.getByEmail(email);
        assertThat(verifications).hasSize(0);
    }

    @DisplayName("Update user information of logged user")
    @Test
    @WithCustomMockUser
    void meUpdate() {
        when(resolveToken.resolve(any())).thenReturn("token");
        when(tokenReader.getAccessTokenInfo(anyString())).thenReturn(new AccessTokenInfo("email@mail.com", 1L, "tenancy", singletonList("ROLE_USER")));

        User user = User.builder()
                .id(1L)
                .userName("userName")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .email("email@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();
        when(userManagementRepository.findByTenancyIdAndEmail(1L, "email@mail.com")).thenReturn(Optional.of(user));

        when(userManagementRepository.existsByTenancyIdAndUserNameAndIdNot(1L, "userName-updated", 1L)).thenReturn(false);

        when(userManagementRepository.save(argThat(u ->
                u.getUserName().equals("userName-updated")
                        && u.getFirstName().equals("firstName-Updated")
                        && u.getLastName().equals("lastName-Updated")
        ))).thenReturn(User.builder()
                .id(1L)
                .userName("userName-updated")
                .password("password")
                .firstName("firstName-Updated")
                .lastName("lastName-Updated")
                .email("email@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build());

        MeUpdate meUpdate = new MeUpdate("userName-updated", "firstName-Updated", "lastName-Updated");
        webTestClient
                .post()
                .uri("/me")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(meUpdate)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserInfo.class)
                .value(response -> {
                    assertThat(response.getUserName()).isEqualTo("userName-updated");
                    assertThat(response.getFirstName()).isEqualTo("firstName-Updated");
                    assertThat(response.getLastName()).isEqualTo("lastName-Updated");
                });
    }

    private static Stream<UserRegistration> feedUsers() {
        return Stream.of(
                new UserRegistration(null,
                        "John",
                        "Wick",
                        "johnwick",
                        "johnwick@mail.com",
                        "dad231#$#4",
                        "dad231#$#4"),
                new UserRegistration(
                        null,
                        "John",
                        "Rambo",
                        "johnrambo",
                        "johnrambo@mail.com",
                        "dad231#$#4",
                        "dad231#$#4"),
                new UserRegistration(
                        null,
                        "Han",
                        "Solo ",
                        "hansolo",
                        "hansolo@mail.com",
                        "dad231#$#4",
                        "dad231#$#4"));
    }
}
