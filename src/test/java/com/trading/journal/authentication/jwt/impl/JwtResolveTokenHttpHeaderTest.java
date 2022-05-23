package com.trading.journal.authentication.jwt.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import com.trading.journal.authentication.jwt.JwtResolveToken;
import com.trading.journal.authentication.jwt.helper.JwtConstants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class JwtResolveTokenHttpHeaderTest {

    JwtResolveToken jwtResolveToken;

    @BeforeEach
    public void setUp() {
        jwtResolveToken = new JwtResolveTokenHttpHeader();
    }

    @Test
    @DisplayName("Given server request with token return token value")
    void requestWithToken() {
        String token = UUID.randomUUID().toString();

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/foo/foo")
                .header(HttpHeaders.AUTHORIZATION, JwtConstants.TOKEN_PREFIX.concat(token)));

        String resolved = jwtResolveToken.resolve(exchange.getRequest());
        assertThat(resolved).isEqualTo(token);
    }

    @Test
    @DisplayName("Given server request without token return null")
    void requestWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/foo/foo"));

        String resolved = jwtResolveToken.resolve(exchange.getRequest());
        assertThat(resolved).isNull();
    }

    @Test
    @DisplayName("Given server request with other header but no token return null")
    void requestWithOtherHeaders() {
        String token = UUID.randomUUID().toString();

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/foo/foo")
                .header(HttpHeaders.ACCEPT, JwtConstants.TOKEN_PREFIX.concat(token))
                .header(HttpHeaders.ACCEPT_LANGUAGE, JwtConstants.TOKEN_PREFIX.concat(token)));

        String resolved = jwtResolveToken.resolve(exchange.getRequest());
        assertThat(resolved).isNull();
    }
}
