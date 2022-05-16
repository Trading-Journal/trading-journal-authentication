package com.trading.journal.authentication.jwt.impl;

import com.trading.journal.authentication.jwt.AccessToken;
import com.trading.journal.authentication.jwt.JwtTokenProvider;

import org.springframework.core.MethodParameter;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class AccessTokenResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider tokenProvider;

    public AccessTokenResolver(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(AccessToken.class) != null;
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext,
            ServerWebExchange exchange) {
        String token = tokenProvider.resolveToken(exchange.getRequest());
        return Mono.just(tokenProvider.getAccessTokenInfo(token));
    }
}
