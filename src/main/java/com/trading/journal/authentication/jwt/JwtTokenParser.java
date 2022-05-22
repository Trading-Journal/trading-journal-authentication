package com.trading.journal.authentication.jwt;

import com.trading.journal.authentication.jwt.data.AccessTokenInfo;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;

public interface JwtTokenParser {
    Authentication getAuthentication(String token);

    AccessTokenInfo getAccessTokenInfo(String token);

    boolean isTokenValid(String token);

    String resolveToken(ServerHttpRequest request);
}
