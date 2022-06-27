package com.trading.journal.authentication.jwt.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.jwt.data.JwtProperties;
import com.trading.journal.authentication.jwt.data.ServiceType;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.jwt.service.JwtTokenProvider;
import com.trading.journal.authentication.jwt.service.PrivateKeyProvider;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.userauthority.UserAuthority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class JwtTokenProviderImplTest {

    @Mock
    PrivateKeyProvider privateKeyProvider;

    JwtTokenProvider tokenProvider;

    @BeforeEach
    public void setUp() throws NoSuchAlgorithmException, IOException {
        JwtProperties properties = new JwtProperties(ServiceType.PROVIDER, new File("arg"), new File("arg"), 3600L,
                86400L, "issuer", "audience");
        when(privateKeyProvider.provide(new File("arg"))).thenReturn(mockPrivateKey());
        tokenProvider = new JwtTokenProviderImpl(properties, privateKeyProvider);
    }

    @Test
    @DisplayName("Given user and its roles when generateAccessToken, return JWT token")
    void generateAccessToken() {
        ApplicationUser appUser = new ApplicationUser(
                1L,
                "UserAdm",
                "123456",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                Collections.singletonList(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))),
                LocalDateTime.now());

        TokenData tokenData = tokenProvider.generateAccessToken(appUser);

        assertThat(tokenData.token()).isNotEmpty();
        assertThat(tokenData.issuedAt()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("Given user and its roles when generateRefreshToken, return JWT token")
    void generateRefreshToken() {
        ApplicationUser appUser = new ApplicationUser(
                1L,
                "UserAdm",
                "123456",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                Collections.singletonList(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))),
                LocalDateTime.now());

        TokenData tokenData = tokenProvider.generateRefreshToken(appUser);

        assertThat(tokenData.token()).isNotEmpty();
        assertThat(tokenData.issuedAt()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("Given email generate a temporary token, return JWT token")
    void generateTemporaryToken() {
        String email = "mail@mail.com";
        TokenData tokenData = tokenProvider.generateTemporaryToken(email);
        assertThat(tokenData.token()).isNotEmpty();
        assertThat(tokenData.issuedAt()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("Given user with null roles when generateAccessToken, return exception")
    void nullRoles() {
        ApplicationUser appUser = new ApplicationUser(
                1L,
                "UserAdm",
                "123456",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                null,
                LocalDateTime.now());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> tokenProvider.generateAccessToken(appUser),
                "User has no authorities");

        assertThat(exception.getRawStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Given user with empty roles when generateAccessToken, return exception")
    void emptyRoles() throws IOException, NoSuchAlgorithmException {
        ApplicationUser appUser = new ApplicationUser(
                1L,
                "UserAdm",
                "123456",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                Collections.emptyList(),
                LocalDateTime.now());

        when(privateKeyProvider.provide(new File("arg"))).thenReturn(mockPrivateKey());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> tokenProvider.generateAccessToken(appUser),
                "User has no authorities");

        assertThat(exception.getRawStatusCode()).isEqualTo(401);
    }

    private PrivateKey mockPrivateKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.genKeyPair();
        return keyPair.getPrivate();
    }
}
