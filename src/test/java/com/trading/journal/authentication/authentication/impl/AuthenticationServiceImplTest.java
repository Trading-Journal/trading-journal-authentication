package com.trading.journal.authentication.authentication.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.jwt.JwtTokenProvider;
import com.trading.journal.authentication.jwt.data.ContextUser;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.emptyList(),
                LocalDateTime.now());
        when(applicationUserService.getUserByEmail(login.email())).thenReturn(Mono.just(applicationUser));

        TokenData tokenData = new TokenData("token", 3600L, Date.from(Instant.now()));
        when(jwtTokenProvider.generateJwtToken(applicationUser)).thenReturn(tokenData);

        Mono<LoginResponse> signIn = authenticationService.signIn(login);
        StepVerifier.create(signIn)
                .assertNext(response -> assertThat(response.token()).isNotBlank()).verifyComplete();

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
        verify(jwtTokenProvider, never()).generateJwtToken(any());
    }
}
