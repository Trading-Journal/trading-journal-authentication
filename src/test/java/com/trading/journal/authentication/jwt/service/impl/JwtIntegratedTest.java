package com.trading.journal.authentication.jwt.service.impl;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.email.service.EmailSender;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.jwt.helper.JwtConstants;
import com.trading.journal.authentication.jwt.service.JwtTokenParser;
import com.trading.journal.authentication.jwt.service.JwtTokenProvider;
import com.trading.journal.authentication.user.ApplicationUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
public class JwtIntegratedTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtTokenParser jwtTokenParser;

    @MockBean
    EmailSender emailSender;

    @BeforeEach
    public void setUp() {
        doNothing().when(emailSender).send(any());
    }

    @DisplayName("Generate and read access token")
    @Test
    void generateAndReadAccessToken() {
        ApplicationUser appUser = new ApplicationUser(
                1L,
                "UserAdm",
                "123456",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                Arrays.asList(new UserAuthority(1L, 1L, 1L, "ROLE_USER"), new UserAuthority(1L, 1L, 1L, "ROLE_ADMIN")),
                LocalDateTime.now());

        TokenData accessToken = jwtTokenProvider.generateAccessToken(appUser);

        assertThat(accessToken.token()).isNotBlank();
        Jws<Claims> accessTokenClaims = jwtTokenParser.parseToken(accessToken.token());
        assertThat(accessTokenClaims.getBody().getSubject()).isEqualTo(appUser.getEmail());
        assertThat(accessTokenClaims.getBody().get(JwtConstants.TENANCY)).isEqualTo(appUser.getUserName());
        assertThat(accessTokenClaims.getBody().getAudience()).isEqualTo("trade-journal");
        assertThat(accessTokenClaims.getBody().getIssuer()).isEqualTo("https://tradejournal.biz");
        Date start = Date.from(LocalDateTime.now().plusSeconds(3500L).atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(LocalDateTime.now().plusSeconds(3600L).atZone(ZoneId.systemDefault()).toInstant());
        assertThat(accessTokenClaims.getBody().getExpiration()).isBetween(start, end);
        List<String> scopes = ((List<?>) accessTokenClaims.getBody().get(JwtConstants.SCOPES))
                .stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .map(SimpleGrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertThat(scopes).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @DisplayName("Generate and read refresh token")
    @Test
    void generateAndReadRefreshToken() {
        ApplicationUser appUser = new ApplicationUser(
                1L,
                "UserAdm",
                "123456",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                Arrays.asList(new UserAuthority(1L, 1L, 1L, "ROLE_USER"), new UserAuthority(1L, 1L, 1L, "ROLE_ADMIN")),
                LocalDateTime.now());

        TokenData refreshToken = jwtTokenProvider.generateRefreshToken(appUser);
        assertThat(refreshToken.token()).isNotBlank();
        Jws<Claims> refreshTokenClaims = jwtTokenParser.parseToken(refreshToken.token());
        assertThat(refreshTokenClaims.getBody().getSubject()).isEqualTo(appUser.getEmail());
        assertThat(refreshTokenClaims.getBody().get(JwtConstants.TENANCY)).isNull();
        assertThat(refreshTokenClaims.getBody().getAudience()).isEqualTo("trade-journal");
        assertThat(refreshTokenClaims.getBody().getIssuer()).isEqualTo("https://tradejournal.biz");
        assertThat(refreshTokenClaims.getBody().getIssuer()).isEqualTo("https://tradejournal.biz");
        Date start = Date.from(LocalDateTime.now().plusSeconds(86300L).atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(LocalDateTime.now().plusSeconds(86400L).atZone(ZoneId.systemDefault()).toInstant());
        assertThat(refreshTokenClaims.getBody().getExpiration()).isBetween(start, end);
        List<String> scopes = ((List<?>) refreshTokenClaims.getBody().get(JwtConstants.SCOPES))
                .stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .map(SimpleGrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertThat(scopes).containsExactly("REFRESH_TOKEN");
    }

    @DisplayName("Generate and read temporary token")
    @Test
    void generateAndReadTemporaryToken() {
        TokenData refreshToken = jwtTokenProvider.generateTemporaryToken("mail@mail.com");
        assertThat(refreshToken.token()).isNotBlank();
        Jws<Claims> refreshTokenClaims = jwtTokenParser.parseToken(refreshToken.token());
        assertThat(refreshTokenClaims.getBody().getSubject()).isEqualTo("mail@mail.com");
        assertThat(refreshTokenClaims.getBody().get(JwtConstants.TENANCY)).isNull();
        assertThat(refreshTokenClaims.getBody().getAudience()).isEqualTo("trade-journal");
        assertThat(refreshTokenClaims.getBody().getIssuer()).isEqualTo("https://tradejournal.biz");
        assertThat(refreshTokenClaims.getBody().getIssuer()).isEqualTo("https://tradejournal.biz");
        Date start = Date.from(LocalDateTime.now().plusSeconds(890).atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(LocalDateTime.now().plusSeconds(905).atZone(ZoneId.systemDefault()).toInstant());
        assertThat(refreshTokenClaims.getBody().getExpiration()).isBetween(start, end);
        List<String> scopes = ((List<?>) refreshTokenClaims.getBody().get(JwtConstants.SCOPES))
                .stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .map(SimpleGrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertThat(scopes).containsExactly("TEMPORARY_TOKEN");
    }
}
