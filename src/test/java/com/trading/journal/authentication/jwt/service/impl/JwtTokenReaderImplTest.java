package com.trading.journal.authentication.jwt.service.impl;

import com.trading.journal.authentication.jwt.data.*;
import com.trading.journal.authentication.jwt.helper.JwtConstants;
import com.trading.journal.authentication.jwt.service.JwtTokenParser;
import com.trading.journal.authentication.jwt.service.JwtTokenReader;
import com.trading.journal.authentication.jwt.service.PublicKeyProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

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
    @DisplayName("Given access token return Authentication with tenancy")
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
                .claim(JwtConstants.TENANCY_ID, 1L)
                .claim(JwtConstants.TENANCY_NAME, "tenancy_1")
                .compact();

        JwtTokenParser mockParser = mockParser(keyPair);
        JwtTokenReader jwtTokenReader = new JwtTokenReaderImpl(mockParser, properties);

        Authentication authentication = jwtTokenReader.getAuthentication(token);
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getCredentials()).isEqualTo(token);
        assertThat(((ContextUser)authentication.getPrincipal()).tenancyName()).isEqualTo("tenancy_1");
        assertThat(((ContextUser)authentication.getPrincipal()).tenancyId()).isEqualTo(1L);
        assertThat(((UsernamePasswordAuthenticationToken) authentication).getAuthorities()).hasSize(1);
        assertThat(authentication.getPrincipal())
                .isInstanceOf(ContextUser.class);
    }

    @Test
    @DisplayName("Given access token without tenancy return Authentication without tenancy")
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

        Authentication authentication = jwtTokenReader.getAuthentication(token);
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getCredentials()).isEqualTo(token);
        assertThat(((ContextUser)authentication.getPrincipal()).tenancyName()).isNull();
        assertThat(((ContextUser)authentication.getPrincipal()).tenancyId()).isNull();
        assertThat(((UsernamePasswordAuthenticationToken) authentication).getAuthorities()).hasSize(1);
        assertThat(authentication.getPrincipal())
                .isInstanceOf(ContextUser.class);
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
                .claim(JwtConstants.TENANCY_ID, 1L)
                .claim(JwtConstants.TENANCY_NAME, "tenancy_1")
                .compact();

        JwtTokenParser mockParser = mockParser(keyPair);
        JwtTokenReader jwtTokenReader = new JwtTokenReaderImpl(mockParser, properties);

        AccessTokenInfo accessTokenInfo = jwtTokenReader.getAccessTokenInfo(token);
        assertThat(accessTokenInfo.subject()).isEqualTo("user_name");
        assertThat(accessTokenInfo.tenancyName()).isEqualTo("tenancy_1");
        assertThat(accessTokenInfo.tenancyId()).isEqualTo(1L);
        assertThat(accessTokenInfo.scopes()).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Given temporary token return Subject")
    void getTemporaryTokenInfo() {
        KeyPair keyPair = mockKeyPair();
        assert keyPair != null;
        String token = Jwts.builder()
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer(properties.getIssuer())
                .setAudience(properties.getAudience())
                .setSubject("email@mail.com")
                .setIssuedAt(Date.from(LocalDateTime.of(2022, Month.APRIL, 1, 13, 14, 15).atZone(ZoneId.systemDefault())
                        .toInstant()))
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(360)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .claim(JwtConstants.SCOPES, Collections.singletonList(JwtConstants.TEMPORARY_TOKEN))
                .compact();

        JwtTokenParser mockParser = mockParser(keyPair);
        JwtTokenReader jwtTokenReader = new JwtTokenReaderImpl(mockParser, properties);

        AccessTokenInfo tokenInfo = jwtTokenReader.getTokenInfo(token);
        assertThat(tokenInfo.subject()).isEqualTo("email@mail.com");
        assertThat(tokenInfo.scopes()).containsExactly("TEMPORARY_TOKEN");
    }

    @Test
    @DisplayName("Given access token without tenancy when getting Token Info without tenancy")
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

        AccessTokenInfo accessTokenInfo = jwtTokenReader.getAccessTokenInfo(token);
        assertThat(accessTokenInfo.subject()).isEqualTo("user_name");
        assertThat(accessTokenInfo.tenancyName()).isNull();
        assertThat(accessTokenInfo.tenancyId()).isNull();
        assertThat(accessTokenInfo.scopes()).containsExactly("ROLE_USER");
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
                .claim(JwtConstants.TENANCY_ID, "tenancy_1")
                .compact();

        JwtTokenParser mockParser = mockParser(keyPair);
        JwtTokenReader jwtTokenReader = new JwtTokenReaderImpl(mockParser, properties);

        boolean tokenValid = jwtTokenReader.isTokenValid(token);
        assertThat(tokenValid).isTrue();
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
                .claim(JwtConstants.TENANCY_ID, "tenancy_1")
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
                .claim(JwtConstants.TENANCY_ID, "tenancy_1")
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

        AccessTokenInfo accessTokenInfo = jwtTokenReader.getTokenInfo(token);
        assertThat(accessTokenInfo.subject()).isEqualTo("user_name");
        assertThat(accessTokenInfo.tenancyName()).isNull();
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
