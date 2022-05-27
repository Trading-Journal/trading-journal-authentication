package com.trading.journal.authentication.authentication.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.jwt.JwtTokenProvider;
import com.trading.journal.authentication.jwt.JwtTokenReader;
import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.jwt.data.ContextUser;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserService;
import com.trading.journal.authentication.user.UserInfo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class AuthenticationServiceImplTest {

    @Mock
    ApplicationUserService applicationUserService;

    @Mock
    ReactiveAuthenticationManager authenticationManager;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    JwtTokenReader jwtTokenReader;

    @InjectMocks
    AuthenticationServiceImpl authenticationService;

    @Test
    @DisplayName("Successfully authenticate a user")
    void testAuthenticateUser() {
        Login login = new Login("mail@mail.com", "123456");

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        ContextUser principal = new ContextUser("mail@mail.com", authorities, "username");
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, authorities);
        when(authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(login.email(), login.password())))
                .thenReturn(Mono.just(authentication));

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "12345679",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.emptyList(),
                LocalDateTime.now());
        when(applicationUserService.getUserByEmail(login.email())).thenReturn(Mono.just(applicationUser));

        TokenData accessToken = new TokenData("token", LocalDateTime.now());
        when(jwtTokenProvider.generateAccessToken(applicationUser)).thenReturn(accessToken);

        TokenData refreshToken = new TokenData("refreshToken", LocalDateTime.now());
        when(jwtTokenProvider.generateRefreshToken(applicationUser)).thenReturn(refreshToken);

        Mono<LoginResponse> signIn = authenticationService.signIn(login);
        StepVerifier.create(signIn)
                .assertNext(response -> {
                    assertThat(response.accessToken()).isEqualTo("token");
                    assertThat(response.refreshToken()).isEqualTo("refreshToken");
                }).verifyComplete();

    }

    @Test
    @DisplayName("When authentication fails throws an exception")
    void testAuthenticateFails() {
        Login login = new Login("mail@mail.com", "123456");
        when(authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(login.email(), login.password())))
                .thenThrow(new AuthenticationServiceException("Authentication failed"));

        assertThrows(AuthenticationServiceException.class, () -> authenticationService.signIn(login),
                "Authentication failed");

        verify(applicationUserService, never()).getUserByEmail(anyString());
        verify(jwtTokenProvider, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Given a valid refresh token returns a new access token and keep the same refresh token")
    void refreshToken() {
        String refreshToken = UUID.randomUUID().toString();

        when(jwtTokenReader.isTokenValid(refreshToken)).thenReturn(true);

        AccessTokenInfo tokenInfo = new AccessTokenInfo("userName", null,
                Collections.singletonList("REFRESH_TOKEN"));
        when(jwtTokenReader.getRefreshTokenInfo(refreshToken)).thenReturn(tokenInfo);

        UserInfo userInfo = new UserInfo(1L,"userName", "firstName", "lastName", "email@mail.com", true, true,
                Collections.singletonList("ROLE_USER"), LocalDateTime.now());
        when(applicationUserService.getUserInfo("userName")).thenReturn(Mono.just(userInfo));

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "12345679",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.emptyList(),
                LocalDateTime.now());
        when(applicationUserService.getUserByEmail("email@mail.com")).thenReturn(Mono.just(applicationUser));

        TokenData tokenData = new TokenData("new_token", LocalDateTime.now());
        when(jwtTokenProvider.generateAccessToken(applicationUser)).thenReturn(tokenData);

        Mono<LoginResponse> response = authenticationService.refreshToken(refreshToken);
        StepVerifier.create(response)
                .assertNext(data -> {
                    assertThat(data.accessToken()).isEqualTo("new_token");
                    assertThat(data.refreshToken()).isEqualTo(refreshToken);
                }).verifyComplete();
    }

    @Test
    @DisplayName("Given a invalid refresh token returns a Unauthorized exception")
    void invalidRefreshToken() {
        String refreshToken = UUID.randomUUID().toString();

        when(jwtTokenReader.isTokenValid(refreshToken)).thenReturn(false);

        Mono<LoginResponse> response = authenticationService.refreshToken(refreshToken);
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && ((ApplicationException) throwable).getStatusCode().equals(HttpStatus.UNAUTHORIZED)
                        && ((ApplicationException) throwable)
                                .getStatusText()
                                .equals("Refresh token is expired"))
                .verify();

        verify(jwtTokenReader, never()).getRefreshTokenInfo(anyString());
        verify(applicationUserService, never()).getUserInfo(anyString());
        verify(applicationUserService, never()).getUserByEmail(anyString());
        verify(jwtTokenProvider, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Given a valid refresh token but the roles are not REFRESH_TOKEN returns a Unauthorized exception")
    void invalidRolesToken() {
        String refreshToken = UUID.randomUUID().toString();

        when(jwtTokenReader.isTokenValid(refreshToken)).thenReturn(true);

        AccessTokenInfo tokenInfo = new AccessTokenInfo("userName", null,
                Collections.singletonList("USER_ROLE"));
        when(jwtTokenReader.getRefreshTokenInfo(refreshToken)).thenReturn(tokenInfo);

        Mono<LoginResponse> response = authenticationService.refreshToken(refreshToken);
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && ((ApplicationException) throwable).getStatusCode().equals(HttpStatus.UNAUTHORIZED)
                        && ((ApplicationException) throwable)
                                .getStatusText()
                                .equals("Refresh token is invalid or is not a refresh token"))
                .verify();

        verify(applicationUserService, never()).getUserInfo(anyString());
        verify(applicationUserService, never()).getUserByEmail(anyString());
        verify(jwtTokenProvider, never()).generateAccessToken(any());
    }
}
