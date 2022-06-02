package com.trading.journal.authentication.jwt;

import java.util.UUID;

import com.trading.journal.authentication.jwt.helper.JwtConstants;

import com.trading.journal.authentication.jwt.service.JwtTokenReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
public class JwtTokenAuthenticationFilterTest {

    @Mock
    JwtTokenReader tokenReader;

    JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter;

    @BeforeEach
    public void setUp() {
        jwtTokenAuthenticationFilter = new JwtTokenAuthenticationFilter(tokenReader);
    }

    @Test
    @DisplayName("Given server request with token process request successfully")
    void serverRequestSuccess() {
        String token = UUID.randomUUID().toString();

        WebFilterChain filterChain = (filterExchange) -> {
            try {
                return Mono.empty();
            } catch (AssertionError ex) {
                return Mono.error(ex);
            }
        };

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/foo/foo")
                .header(HttpHeaders.AUTHORIZATION, JwtConstants.TOKEN_PREFIX.concat(token)));

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
