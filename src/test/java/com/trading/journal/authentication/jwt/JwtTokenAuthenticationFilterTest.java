package com.trading.journal.authentication.jwt;

import java.time.LocalDateTime;
import java.util.Collections;

import com.trading.journal.authentication.jwt.impl.JwtTokenProviderImpl;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.Authority;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
public class JwtTokenAuthenticationFilterTest {
    JwtTokenProvider tokenProvider;

    JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter;

    @BeforeEach
    public void setUp() {
        tokenProvider = new JwtTokenProviderImpl();
        jwtTokenAuthenticationFilter = new JwtTokenAuthenticationFilter(tokenProvider);
    }

    @Test
    @DisplayName("Given server request with token process request successfully")
    void serverRequestSuccess() {
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

        WebFilterChain filterChain = (filterExchange) -> {
            try {
                return Mono.empty();
            } catch (AssertionError ex) {
                return Mono.error(ex);
            }
        };

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/foo/foo")
                .header(HttpHeaders.AUTHORIZATION, JwtConstantsHelper.TOKEN_PREFIX.concat(tokenData.token())));
        jwtTokenAuthenticationFilter.filter(exchange, filterChain).block();
    }

    @Test
    @DisplayName("Given server request without token process request successfully")
    void serverRequestSuccessWithoutToken() {
        WebFilterChain filterChain = (filterExchange) -> {
            try {
                return Mono.empty();
            } catch (AssertionError ex) {
                return Mono.error(ex);
            }
        };

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/foo/foo"));
        jwtTokenAuthenticationFilter.filter(exchange, filterChain).block();
    }
}
