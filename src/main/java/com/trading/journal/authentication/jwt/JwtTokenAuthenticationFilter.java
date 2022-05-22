package com.trading.journal.authentication.jwt;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class JwtTokenAuthenticationFilter implements WebFilter {

    private final JwtTokenParser tokenParser;

    public JwtTokenAuthenticationFilter(JwtTokenParser tokenParser) {
        this.tokenParser = tokenParser;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = tokenParser.resolveToken(exchange.getRequest());
        Mono<Void> monoChain;
        if (StringUtils.hasText(token) && this.tokenParser.isTokenValid(token)) {
            Authentication authentication = this.tokenParser.getAuthentication(token);
            monoChain = chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        } else {
            monoChain = chain.filter(exchange);
        }
        return monoChain;
    }
}
