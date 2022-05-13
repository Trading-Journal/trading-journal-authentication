package com.trading.journal.authentication.jwt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.trading.journal.authentication.configuration.AuthoritiesHelper;
import com.trading.journal.authentication.jwt.DateHelper;
import com.trading.journal.authentication.jwt.JwtConstantsHelper;
import com.trading.journal.authentication.jwt.JwtTokenProvider;
import com.trading.journal.authentication.jwt.TokenData;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.Authority;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtTokenProviderImplTest {
    private final static String TOKEN_SECRET = "1_sEQNtLZ33v4Ynye4tQ8pJ8lOhmjMNEs7XI-nJ0s6lKjyTMHmK7Gpfnz1xQmoF6zSlQMe4t34wua-YHeX4aCj3W5q9Ty3MPP7I1ULC3B9InNq8Y4_SpwciizpH7wsUlfEO1VAtV6MxSXhBaoYY1yI4UWRYvtAMH_idWiIA-y25x1KBF5slm9ry6DZa5t0mFpXzqFXjsrcxF724B_zKl--Ka-yG_jDdD-iPxyr8EWOIZgs2TVkgAn_jZ3-1VvH-HPvtCBrDdbVAc4NVK-o04Uyf2y-Fb72naYQbfFLkMk9_NCIpG6TpGeEGQR9e5wO0A87mzEGtTHDAV85WE5uXDw";

    JwtTokenProvider tokenProvider;

    @BeforeEach
    public void setUp() {
        tokenProvider = new JwtTokenProviderImpl();
    }

    @Test
    @DisplayName("Given user and its roles when generateJwtToken, return JWT token")
    void given_user_and_roles_return_token() {
        ApplicationUser appUser = new ApplicationUser(
                "UserAdm",
                "123456",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                Collections.singletonList(new Authority("ROLE_USER")),
                LocalDateTime.now());

        TokenData tokenData = tokenProvider.generateJwtToken(appUser);

        assertThat(tokenData.token()).isNotEmpty();
        assertThat(tokenData.expirationIn()).isEqualTo(3600L);
        assertThat(tokenData.issuedAt()).isBefore(Date.from(Instant.now()));
    }

    @Test
    @DisplayName("Given user and its roles when getRoles, return same roles")
    void getRoles() {
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

        TokenData tokenData = tokenProvider.generateJwtToken(appUser);
        List<String> roles = tokenProvider.getRoles(tokenData.token());
        assertThat(roles).containsOnly(AuthoritiesHelper.ROLE_ADMIN, AuthoritiesHelper.ROLE_USER);
    }

    @Test
    @DisplayName("Given user with null roles when generateJwtToken, return exception")
    void nullRoles() {
        ApplicationUser appUser = new ApplicationUser(
                "UserAdm",
                "123456",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                null,
                LocalDateTime.now());

        assertThrows(
                AuthenticationServiceException.class,
                () -> tokenProvider.generateJwtToken(appUser),
                "User has not authority roles");
    }

    @Test
    @DisplayName("Given user with empty roles when generateJwtToken, return exception")
    void emptyRoles() {
        ApplicationUser appUser = new ApplicationUser(
                "UserAdm",
                "123456",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                Collections.emptyList(),
                LocalDateTime.now());

        assertThrows(
                AuthenticationServiceException.class,
                () -> tokenProvider.generateJwtToken(appUser),
                "User has not authority roles");
    }

    @Test
    @DisplayName("Given expired jwt token when validateToken return invalid")
    void expiredToken() {
        String userName = "user";
        Date issuedAt = DateHelper.getUTCDatetimeAsDate();
        Date expiration = Date.from(LocalDateTime.now().minusSeconds(1).atZone(ZoneId.systemDefault()).toInstant());

        String token = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(TOKEN_SECRET.getBytes()), SignatureAlgorithm.HS512)
                .setHeaderParam(JwtConstantsHelper.HEADER_TYP, JwtConstantsHelper.TOKEN_TYPE)
                .setIssuer(JwtConstantsHelper.TOKEN_ISSUER)
                .setAudience(JwtConstantsHelper.TOKEN_AUDIENCE)
                .setSubject(userName)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .claim(JwtConstantsHelper.AUTHORITIES, Collections.singletonList(AuthoritiesHelper.ROLE_USER))
                .compact();

        boolean isTokenValid = tokenProvider.validateToken(token);

        assertThat(isTokenValid).isFalse();
    }

    @Test
    @DisplayName("Given almost expired jwt token when validateToken return valid")
    void almostExpiredToken() {
        String userName = "user";
        Date issuedAt = DateHelper.getUTCDatetimeAsDate();
        Date expiration = Date.from(LocalDateTime.now().plusSeconds(3).atZone(ZoneId.systemDefault()).toInstant());

        String token = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(TOKEN_SECRET.getBytes()), SignatureAlgorithm.HS512)
                .setHeaderParam(JwtConstantsHelper.HEADER_TYP, JwtConstantsHelper.TOKEN_TYPE)
                .setIssuer(JwtConstantsHelper.TOKEN_ISSUER)
                .setAudience(JwtConstantsHelper.TOKEN_AUDIENCE)
                .setSubject(userName)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .claim(JwtConstantsHelper.AUTHORITIES, Collections.singletonList(AuthoritiesHelper.ROLE_USER))
                .compact();

        boolean isTokenValid = tokenProvider.validateToken(token);

        assertThat(isTokenValid).isTrue();
    }

    @Test
    @DisplayName("Given token when get authentication return Principal data")
    void getAuthentication() {
        ApplicationUser appUser = new ApplicationUser(
                "UserAdm",
                "123456",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                Collections.singletonList(new Authority("ROLE_USER")),
                LocalDateTime.now());

        var tokenData = tokenProvider.generateJwtToken(appUser);
        Authentication authentication = tokenProvider.getAuthentication(tokenData.token());

        assertThat(((User) authentication.getPrincipal()).getUsername()).isEqualTo("UserAdm");
        assertThat(authentication.getAuthorities().toArray()).contains(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
