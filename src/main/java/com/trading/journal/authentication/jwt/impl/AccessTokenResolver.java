package com.trading.journal.authentication.jwt.impl;

import com.trading.journal.authentication.jwt.JwtTokenParser;
import com.trading.journal.authentication.jwt.data.AccessToken;

import org.springframework.core.MethodParameter;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class AccessTokenResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenParser tokenParser;

    public AccessTokenResolver(JwtTokenParser tokenParser) {
        this.tokenParser = tokenParser;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(AccessToken.class) != null;
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext,
            ServerWebExchange exchange) {
        String token = tokenParser.resolveToken(exchange.getRequest());
        return Mono.just(tokenParser.getAccessTokenInfo(token));
    }
}
