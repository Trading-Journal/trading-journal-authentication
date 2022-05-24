package com.trading.journal.authentication.jwt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.jwt.JwtTokenProvider;
import com.trading.journal.authentication.jwt.PrivateKeyProvider;
import com.trading.journal.authentication.jwt.data.JwtProperties;
import com.trading.journal.authentication.jwt.data.ServiceType;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.Authority;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class JwtTokenProviderImplTest {

    @Mock
    PrivateKeyProvider privateKeyProvider;

    JwtTokenProvider tokenProvider;

    @BeforeEach
    public void setUp() {
        JwtProperties properties = new JwtProperties(ServiceType.PROVIDER, new File("arg"), new File("arg"), 3600L,
                86400L);
        tokenProvider = new JwtTokenProviderImpl(properties, privateKeyProvider);
    }

    @Test
    @DisplayName("Given user and its roles when generateJwtToken, return JWT token")
    void given_user_and_roles_return_token() throws NoSuchAlgorithmException, IOException {
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

        when(privateKeyProvider.provide(new File("arg"))).thenReturn(mockPrivateKey());

        TokenData tokenData = tokenProvider.generateJwtToken(appUser);

        assertThat(tokenData.accessToken()).isNotEmpty();
        assertThat(tokenData.refreshToken()).isNotEmpty();
        assertThat(tokenData.expirationIn()).isEqualTo(3600L);
        assertThat(tokenData.issuedAt()).isBefore(Date.from(Instant.now()));
    }

    @Test
    @DisplayName("Given user with null roles when generateJwtToken, return exception")
    void nullRoles() throws IOException, NoSuchAlgorithmException {
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

        when(privateKeyProvider.provide(new File("arg"))).thenReturn(mockPrivateKey());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> tokenProvider.generateJwtToken(appUser),
                "User has not authority roles");

        assertThat(exception.getRawStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Given user with empty roles when generateJwtToken, return exception")
    void emptyRoles() throws IOException, NoSuchAlgorithmException {
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

        when(privateKeyProvider.provide(new File("arg"))).thenReturn(mockPrivateKey());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> tokenProvider.generateJwtToken(appUser),
                "User has not authority roles");

        assertThat(exception.getRawStatusCode()).isEqualTo(401);
    }

    private PrivateKey mockPrivateKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.genKeyPair();
        return keyPair.getPrivate();
    }
}
