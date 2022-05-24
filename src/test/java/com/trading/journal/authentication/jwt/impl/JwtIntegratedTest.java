package com.trading.journal.authentication.jwt.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.trading.journal.authentication.MongoInitializer;
import com.trading.journal.authentication.jwt.JwtTokenParser;
import com.trading.journal.authentication.jwt.JwtTokenProvider;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.jwt.helper.JwtConstants;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.Authority;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MongoInitializer.class)
public class JwtIntegratedTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtTokenParser jwtTokenParser;

    @DisplayName("Generate and read tokens")
    @Test
    void provider() {
        ApplicationUser appUser = new ApplicationUser(
                "UserAdm",
                "123456",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                Arrays.asList(new Authority("ROLE_USER"), new Authority("ROLE_ADMIN")),
                LocalDateTime.now());

        TokenData accessToken = jwtTokenProvider.generateAccessToken(appUser);

        assertThat(accessToken.token()).isNotBlank();
        Jws<Claims> accessTokenClaims = jwtTokenParser.parseToken(accessToken.token());
        assertThat(accessTokenClaims.getBody().getSubject()).isEqualTo(appUser.userName());
        assertThat(accessTokenClaims.getBody().get(JwtConstants.TENANCY)).isEqualTo(appUser.userName());
        assertThat(accessTokenClaims.getBody().getAudience()).isEqualTo("trade-journal");
        assertThat(accessTokenClaims.getBody().getIssuer()).isEqualTo("https://tradejournal.biz");
        Date start = Date.from(LocalDateTime.now().plusSeconds(3500L).atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(LocalDateTime.now().plusSeconds(3600L).atZone(ZoneId.systemDefault()).toInstant());
        assertThat(accessTokenClaims.getBody().getExpiration()).isBetween(start, end);
        List<String> scopes = ((List<?>) accessTokenClaims.getBody().get(JwtConstants.SCOPES))
                .stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .map(arg0 -> arg0.getAuthority())
                .collect(Collectors.toList());
        assertThat(scopes).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");

        TokenData refreshToken = jwtTokenProvider.generateRefreshToken(appUser);
        assertThat(refreshToken.token()).isNotBlank();
        Jws<Claims> refreshTokenClaims = jwtTokenParser.parseToken(refreshToken.token());
        assertThat(refreshTokenClaims.getBody().getSubject()).isEqualTo(appUser.userName());
        assertThat(refreshTokenClaims.getBody().get(JwtConstants.TENANCY)).isNull();
        assertThat(refreshTokenClaims.getBody().getAudience()).isNull();
        assertThat(refreshTokenClaims.getBody().getIssuer()).isEqualTo("https://tradejournal.biz");
        start = Date.from(LocalDateTime.now().plusSeconds(86300L).atZone(ZoneId.systemDefault()).toInstant());
        end = Date.from(LocalDateTime.now().plusSeconds(86400L).atZone(ZoneId.systemDefault()).toInstant());
        assertThat(refreshTokenClaims.getBody().getExpiration()).isBetween(start, end);
        scopes = ((List<?>) refreshTokenClaims.getBody().get(JwtConstants.SCOPES))
                .stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .map(arg0 -> arg0.getAuthority())
                .collect(Collectors.toList());
        assertThat(scopes).containsExactly("REFRESH_TOKEN");
    }
}
