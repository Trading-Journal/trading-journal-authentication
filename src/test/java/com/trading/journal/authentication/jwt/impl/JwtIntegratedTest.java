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

        TokenData tokens = jwtTokenProvider.generateJwtToken(appUser);

        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
        Jws<Claims> accessToken = jwtTokenParser.parseToken(tokens.accessToken());
        assertThat(accessToken.getBody().getSubject()).isEqualTo(appUser.userName());
        assertThat(accessToken.getBody().get(JwtConstants.TENANCY)).isEqualTo(appUser.userName());
        assertThat(accessToken.getBody().getAudience()).isEqualTo("trade-journal");
        assertThat(accessToken.getBody().getIssuer()).isEqualTo("https://tradejournal.biz");
        Date start = Date.from(LocalDateTime.now().plusSeconds(3500L).atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(LocalDateTime.now().plusSeconds(3600L).atZone(ZoneId.systemDefault()).toInstant());
        assertThat(accessToken.getBody().getExpiration()).isBetween(start, end);
        List<String> scopes = ((List<?>) accessToken.getBody().get(JwtConstants.SCOPES))
                .stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .map(arg0 -> arg0.getAuthority())
                .collect(Collectors.toList());
        assertThat(scopes).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");

        Jws<Claims> refreshToken = jwtTokenParser.parseToken(tokens.refreshToken());
        assertThat(refreshToken.getBody().getSubject()).isEqualTo(appUser.userName());
        assertThat(refreshToken.getBody().get(JwtConstants.TENANCY)).isNull();
        assertThat(refreshToken.getBody().getAudience()).isNull();
        assertThat(refreshToken.getBody().getIssuer()).isEqualTo("https://tradejournal.biz");
        start = Date.from(LocalDateTime.now().plusSeconds(86300L).atZone(ZoneId.systemDefault()).toInstant());
        end = Date.from(LocalDateTime.now().plusSeconds(86400L).atZone(ZoneId.systemDefault()).toInstant());
        assertThat(refreshToken.getBody().getExpiration()).isBetween(start, end);
        scopes = ((List<?>) refreshToken.getBody().get(JwtConstants.SCOPES))
                .stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .map(arg0 -> arg0.getAuthority())
                .collect(Collectors.toList());
        assertThat(scopes).containsExactly("REFRESH_TOKEN");
    }
}
