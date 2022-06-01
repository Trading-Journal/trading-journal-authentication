package com.trading.journal.authentication.verification.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.jwt.service.JwtTokenProvider;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.verification.service.HashProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Arrays;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
class HashProviderJwtTest {

    @Autowired
    private HashProvider hashProvider;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @DisplayName("Hash email into jwt and read the same email")
    @Test
    void hashAndRead() {
        String hash = hashProvider.generateHash("user@mail.com.cz");
        assertThat(hash).isNotBlank();

        String email = hashProvider.readHashValue(hash);
        assertThat(email).isEqualTo("user@mail.com.cz");
    }

    @DisplayName("Given an invalid hash token throws an exception")
    @Test
    void invalidToken() {
        ApplicationUser applicationUser = new ApplicationUser(
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

        TokenData tokenData = jwtTokenProvider.generateAccessToken(applicationUser);

        assertThrows(ApplicationException.class, () -> hashProvider.readHashValue(tokenData.token()),
                "Hash is not in the right format");
    }
}