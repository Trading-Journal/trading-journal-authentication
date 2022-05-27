package com.trading.journal.authentication.jwt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import com.trading.journal.authentication.jwt.JwtTokenParser;
import com.trading.journal.authentication.jwt.JwtTokenReader;
import com.trading.journal.authentication.jwt.PublicKeyProvider;
import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.jwt.data.ContextUser;
import com.trading.journal.authentication.jwt.data.JwtProperties;
import com.trading.journal.authentication.jwt.data.ServiceType;
import com.trading.journal.authentication.jwt.helper.JwtConstants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@ExtendWith(SpringExtension.class)
public class JwtTokenReaderImplTest {

    @Mock
    PublicKeyProvider publicKeyProvider;

    @Mock
    JwtProperties properties;

    @Mock
    JwtTokenParser tokenParser;

    @BeforeEach
    void setUp() {
        when(properties.getIssuer()).thenReturn("TOKEN_ISSUER");
        when(properties.getAudience()).thenReturn("TOKEN_AUDIENCE");
    }

    @Test
    @DisplayName("Given access token return Authentication")
    void authentication() {
        KeyPair keyPair = mockKeyPair();
        assert keyPair != null;
        String token = Jwts.builder()
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer("TOKEN_ISSUER")
                .setAudience("TOKEN_AUDIENCE")
                .setSubject("user_name")
                .setIssuedAt(Date.from(LocalDateTime.of(2022, Month.APRIL, 1, 13, 14, 15).atZone(ZoneId.systemDefault())
                        .toInstant()))
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(360)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .claim(JwtConstants.SCOPES, Collections.singleton("ROLE_USER"))
                .claim(JwtConstants.TENANCY, "tenancy_1")
                .compact();

        JwtTokenParser mockParser = mockParser(keyPair);
        JwtTokenReader jwtTokenReader = new JwtTokenReaderImpl(mockParser, properties);

        Authentication authentication = jwtTokenReader.getAuthentication(token);
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getCredentials()).isEqualTo(token);
        assertThat(((UsernamePasswordAuthenticationToken) authentication).getAuthorities()).hasSize(1);
        assertThat(authentication.getPrincipal())
                .isInstanceOf(ContextUser.class);
    }

    @Test
    @DisplayName("Given access token without tenancy when getting Authentication return exception")
    void authenticationException() {
        KeyPair keyPair = mockKeyPair();
        assert keyPair != null;
        String token = Jwts.builder()
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer("TOKEN_ISSUER")
                .setAudience("TOKEN_AUDIENCE")
                .setSubject("user_name")
                .setIssuedAt(Date.from(LocalDateTime.of(2022, Month.APRIL, 1, 13, 14, 15).atZone(ZoneId.systemDefault())
                        .toInstant()))
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(360)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .claim(JwtConstants.SCOPES, Collections.singleton("ROLE_USER"))
                .compact();

        JwtTokenParser mockParser = mockParser(keyPair);
        JwtTokenReader jwtTokenReader = new JwtTokenReaderImpl(mockParser, properties);

        AuthenticationServiceException exception = assertThrows(AuthenticationServiceException.class,
                () -> jwtTokenReader.getAuthentication(token));
        assertThat(exception.getMessage()).contains("User tenancy not found inside the token");
    }

    @Test
    @DisplayName("Given access token when getting Token Info return info")
    void accessTokenInfo() {
        KeyPair keyPair = mockKeyPair();
        assert keyPair != null;
        String token = Jwts.builder()
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer("TOKEN_ISSUER")
                .setAudience("TOKEN_AUDIENCE")
                .setSubject("user_name")
                .setIssuedAt(Date.from(LocalDateTime.of(2022, Month.APRIL, 1, 13, 14, 15).atZone(ZoneId.systemDefault())
                        .toInstant()))
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(360)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .claim(JwtConstants.SCOPES, Collections.singleton("ROLE_USER"))
                .claim(JwtConstants.TENANCY, "tenancy_1")
                .compact();

        JwtTokenParser mockParser = mockParser(keyPair);
        JwtTokenReader jwtTokenReader = new JwtTokenReaderImpl(mockParser, properties);

        AccessTokenInfo accessTokenInfo = jwtTokenReader.getAccessTokenInfo(token);
        assertThat(accessTokenInfo.userName()).isEqualTo("user_name");
        assertThat(accessTokenInfo.tenancy()).isEqualTo("tenancy_1");
        assertThat(accessTokenInfo.scopes()).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Given access token without tenancy when getting Token Info return exception")
    void accessTokenInfoException() {
        KeyPair keyPair = mockKeyPair();
        assert keyPair != null;
        String token = Jwts.builder()
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer("TOKEN_ISSUER")
                .setAudience("TOKEN_AUDIENCE")
                .setSubject("user_name")
                .setIssuedAt(Date.from(LocalDateTime.of(2022, Month.APRIL, 1, 13, 14, 15).atZone(ZoneId.systemDefault())
                        .toInstant()))
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(360)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .claim(JwtConstants.SCOPES, Collections.singleton("ROLE_USER"))
                .compact();

        JwtTokenParser mockParser = mockParser(keyPair);
        JwtTokenReader jwtTokenReader = new JwtTokenReaderImpl(mockParser, properties);

        AuthenticationServiceException exception = assertThrows(AuthenticationServiceException.class,
                () -> jwtTokenReader.getAccessTokenInfo(token));
        assertThat(exception.getMessage()).contains("User tenancy not found inside the token");
    }

    @Test
    @DisplayName("Given access token return it is valid")
    void validToken() {
        KeyPair keyPair = mockKeyPair();
        assert keyPair != null;
        String token = Jwts.builder()
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer("TOKEN_ISSUER")
                .setAudience("TOKEN_AUDIENCE")
                .setSubject("user_name")
                .setIssuedAt(Date.from(LocalDateTime.of(2022, Month.APRIL, 1, 13, 14, 15).atZone(ZoneId.systemDefault())
                        .toInstant()))
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(360)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .claim(JwtConstants.SCOPES, Collections.singleton("ROLE_USER"))
                .claim(JwtConstants.TENANCY, "tenancy_1")
                .compact();

        JwtTokenParser mockParser = mockParser(keyPair);
        JwtTokenReader jwtTokenReader = new JwtTokenReaderImpl(mockParser, properties);

        boolean tokenValid = jwtTokenReader.isTokenValid(token);
        assertThat(tokenValid).isTrue();
    }

    @Test
    @DisplayName("Given access token return it is invalid because it is expired 2 seconds ago")
    void invalidToken() {
        KeyPair keyPair = mockKeyPair();
        assert keyPair != null;
        String token = Jwts.builder()
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer("TOKEN_ISSUER")
                .setAudience("TOKEN_AUDIENCE")
                .setSubject("user_name")
                .setIssuedAt(Date.from(LocalDateTime.of(2022, Month.APRIL, 1, 13, 14, 15).atZone(ZoneId.systemDefault())
                        .toInstant()))
                .setExpiration(Date.from(LocalDateTime.now().minusSeconds(2)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .claim(JwtConstants.SCOPES, Collections.singleton("ROLE_USER"))
                .claim(JwtConstants.TENANCY, "tenancy_1")
                .compact();

        JwtTokenParser mockParser = mockParser(keyPair);
        JwtTokenReader jwtTokenReader = new JwtTokenReaderImpl(mockParser, properties);

        boolean tokenValid = jwtTokenReader.isTokenValid(token);
        assertThat(tokenValid).isFalse();
    }

    @Test
    @DisplayName("Given access token return it is invalid because it has different issuer")
    void invalidTokenIssuer() {
        KeyPair keyPair = mockKeyPair();
        assert keyPair != null;
        String token = Jwts.builder()
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer("another_issuer")
                .setAudience("TOKEN_AUDIENCE")
                .setSubject("user_name")
                .setIssuedAt(Date.from(LocalDateTime.of(2022, Month.APRIL, 1, 13, 14, 15).atZone(ZoneId.systemDefault())
                        .toInstant()))
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(360)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .claim(JwtConstants.SCOPES, Collections.singleton("ROLE_USER"))
                .claim(JwtConstants.TENANCY, "tenancy_1")
                .compact();

        JwtTokenParser mockParser = mockParser(keyPair);
        JwtTokenReader jwtTokenReader = new JwtTokenReaderImpl(mockParser, properties);

        boolean tokenValid = jwtTokenReader.isTokenValid(token);
        assertThat(tokenValid).isFalse();
    }

    @Test
    @DisplayName("Given access token return it is invalid because it has different audience")
    void invalidTokenAudience() {
        KeyPair keyPair = mockKeyPair();
        assert keyPair != null;
        String token = Jwts.builder()
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer("TOKEN_ISSUER")
                .setAudience("another_audience")
                .setSubject("user_name")
                .setIssuedAt(Date.from(LocalDateTime.of(2022, Month.APRIL, 1, 13, 14, 15).atZone(ZoneId.systemDefault())
                        .toInstant()))
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(360)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .claim(JwtConstants.SCOPES, Collections.singleton("ROLE_USER"))
                .claim(JwtConstants.TENANCY, "tenancy_1")
                .compact();

        JwtTokenParser mockParser = mockParser(keyPair);
        JwtTokenReader jwtTokenReader = new JwtTokenReaderImpl(mockParser, properties);

        boolean tokenValid = jwtTokenReader.isTokenValid(token);
        assertThat(tokenValid).isFalse();
    }

    @Test
    @DisplayName("Given refresh token when getting Token Info return info")
    void refreshTokenInfo() {
        KeyPair keyPair = mockKeyPair();
        assert keyPair != null;
        String token = Jwts.builder()
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer("TOKEN_ISSUER")
                .setAudience("TOKEN_AUDIENCE")
                .setSubject("user_name")
                .setIssuedAt(Date.from(LocalDateTime.of(2022, Month.APRIL, 1, 13, 14, 15).atZone(ZoneId.systemDefault())
                        .toInstant()))
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(360)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .claim(JwtConstants.SCOPES, Collections.singletonList("REFRESH_TOKEN"))
                .compact();

        JwtTokenParser mockParser = mockParser(keyPair);
        JwtTokenReader jwtTokenReader = new JwtTokenReaderImpl(mockParser, properties);

        AccessTokenInfo accessTokenInfo = jwtTokenReader.getRefreshTokenInfo(token);
        assertThat(accessTokenInfo.userName()).isEqualTo("user_name");
        assertThat(accessTokenInfo.tenancy()).isNull();
        assertThat(accessTokenInfo.scopes()).containsExactly("REFRESH_TOKEN");
    }

    private JwtTokenParser mockParser(KeyPair keyPair) {
        JwtProperties properties = new JwtProperties(ServiceType.PROVIDER, new File("arg"), new File("arg"), 3600L,
                86400L, "issuer", "audience");
        try {
            when(publicKeyProvider.provide(new File("arg"))).thenReturn(keyPair.getPublic());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JwtTokenParserImpl(publicKeyProvider, properties);
    }

    private KeyPair mockKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            return keyGen.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
