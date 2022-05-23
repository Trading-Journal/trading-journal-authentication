package com.trading.journal.authentication.jwt;

import com.trading.journal.authentication.jwt.impl.JwtResolveTokenHttpHeader;

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

    private final JwtTokenReader tokenReader;
    private final JwtResolveToken resolveToken;

    public JwtTokenAuthenticationFilter(JwtTokenReader tokenReader) {
        this.tokenReader = tokenReader;
        this.resolveToken = new JwtResolveTokenHttpHeader();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = resolveToken.resolve(exchange.getRequest());
        Mono<Void> monoChain;
        if (StringUtils.hasText(token) && this.tokenReader.isTokenValid(token)) {
            Authentication authentication = this.tokenReader.getAuthentication(token);
            monoChain = chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        } else {
            monoChain = chain.filter(exchange);
        }
        return monoChain;
    }
}
