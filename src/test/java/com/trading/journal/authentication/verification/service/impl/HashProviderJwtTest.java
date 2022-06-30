package com.trading.journal.authentication.verification.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.jwt.service.JwtTokenProvider;
import com.trading.journal.authentication.jwt.service.JwtTokenReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class HashProviderJwtTest {

    @Mock
    private JwtTokenReader jwtTokenReader;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    HashProviderJwt hashProvider;

    @DisplayName("Hash email into jwt")
    @Test
    void hashAValue() {
        when(jwtTokenProvider.generateTemporaryToken(anyString())).thenReturn(new TokenData("123", LocalDateTime.now()));
        String hash = hashProvider.generateHash("mail");
        assertThat(hash).isEqualTo("123");
    }

    @DisplayName("Read a hash jwt string")
    @Test
    void readHash() {
        String hash = "123";
        when(jwtTokenReader.isTokenValid(hash)).thenReturn(true);
        when(jwtTokenReader.getTokenInfo(hash)).thenReturn(new AccessTokenInfo("subject", "tenancy", singletonList("TEMPORARY_TOKEN")));
        String value = hashProvider.readHashValue(hash);
        assertThat(value).isEqualTo("subject");
    }

    @DisplayName("Read a hash jwt token is invalid return exception")
    @Test
    void readHashInvalidToken() {
        String hash = "123";
        when(jwtTokenReader.isTokenValid(hash)).thenReturn(false);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> hashProvider.readHashValue(hash));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Invalid hash value");

        verify(jwtTokenReader, never()).getTokenInfo(anyString());
    }

    @DisplayName("Read a hash jwt token scopes > 1 return exception")
    @Test
    void readHashScopesGreaterThan1() {
        String hash = "123";
        when(jwtTokenReader.isTokenValid(hash)).thenReturn(true);
        when(jwtTokenReader.getTokenInfo(hash)).thenReturn(
                new AccessTokenInfo("subject", "tenancy", asList("TEMPORARY_TOKEN", "ANOTHER"))
        );

        ApplicationException exception = assertThrows(ApplicationException.class, () -> hashProvider.readHashValue(hash));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Hash is not in the right format");
    }

    @DisplayName("Read a hash jwt token scopes == 1 but none is TEMPORARY_TOKEN return exception")
    @Test
    void readHashScopesNotTEMPORARY_TOKEN() {
        String hash = "123";
        when(jwtTokenReader.isTokenValid(hash)).thenReturn(true);
        when(jwtTokenReader.getTokenInfo(hash)).thenReturn(
                new AccessTokenInfo("subject", "tenancy", singletonList("ANOTHER"))
        );

        ApplicationException exception = assertThrows(ApplicationException.class, () -> hashProvider.readHashValue(hash));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Hash is not in the right format");
    }

    @DisplayName("Read a hash jwt token info is null return exception")
    @Test
    void readHashTokenInfoNull() {
        String hash = "123";
        when(jwtTokenReader.isTokenValid(hash)).thenReturn(true);
        when(jwtTokenReader.getTokenInfo(hash)).thenReturn(null);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> hashProvider.readHashValue(hash));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Hash is not in the right format");
    }

    @DisplayName("Read a hash jwt token scopes empty return exception")
    @Test
    void readHashScopeEmpty() {
        String hash = "123";
        when(jwtTokenReader.isTokenValid(hash)).thenReturn(true);
        when(jwtTokenReader.getTokenInfo(hash)).thenReturn(
                new AccessTokenInfo("subject", "tenancy", emptyList())
        );

        ApplicationException exception = assertThrows(ApplicationException.class, () -> hashProvider.readHashValue(hash));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Hash is not in the right format");
    }

    @DisplayName("Read a hash jwt token scopes null return exception")
    @Test
    void readHashScopeNull() {
        String hash = "123";
        when(jwtTokenReader.isTokenValid(hash)).thenReturn(true);
        when(jwtTokenReader.getTokenInfo(hash)).thenReturn(
                new AccessTokenInfo("subject", "tenancy", null)
        );

        ApplicationException exception = assertThrows(ApplicationException.class, () -> hashProvider.readHashValue(hash));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Hash is not in the right format");
    }

    @DisplayName("Read a hash jwt token subject null return exception")
    @Test
    void readHashSubjectNull() {
        String hash = "123";
        when(jwtTokenReader.isTokenValid(hash)).thenReturn(true);
        when(jwtTokenReader.getTokenInfo(hash)).thenReturn(
                new AccessTokenInfo(null, "tenancy", singletonList("TEMPORARY_TOKEN"))
        );
        ApplicationException exception = assertThrows(ApplicationException.class, () -> hashProvider.readHashValue(hash));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Invalid hash content");
    }
}