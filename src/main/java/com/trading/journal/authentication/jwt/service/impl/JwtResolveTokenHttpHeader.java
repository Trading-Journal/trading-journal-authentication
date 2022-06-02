package com.trading.journal.authentication.jwt.service.impl;

import com.trading.journal.authentication.jwt.service.JwtResolveToken;
import com.trading.journal.authentication.jwt.helper.JwtConstants;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtResolveTokenHttpHeader implements JwtResolveToken {

    @Override
    public String resolve(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtConstants.TOKEN_PREFIX)) {
            token = bearerToken.replace(JwtConstants.TOKEN_PREFIX, "");
        }
        return token;
    }

}
