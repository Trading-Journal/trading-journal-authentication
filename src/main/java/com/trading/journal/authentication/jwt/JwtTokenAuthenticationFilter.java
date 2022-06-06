package com.trading.journal.authentication.jwt;

import com.trading.journal.authentication.jwt.service.JwtResolveToken;
import com.trading.journal.authentication.jwt.service.JwtTokenReader;
import com.trading.journal.authentication.jwt.service.impl.JwtResolveTokenHttpHeader;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenReader tokenReader;
    private final JwtResolveToken resolveToken;

    public JwtTokenAuthenticationFilter(JwtTokenReader tokenReader) {
        this.tokenReader = tokenReader;
        this.resolveToken = new JwtResolveTokenHttpHeader();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var authentication = getAuthentication(request);
        if (authentication == null) {
            filterChain.doFilter(request, response);
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = null;
        String token = resolveToken.resolve(request);
        if (StringUtils.hasText(token) && this.tokenReader.isTokenValid(token)) {
            usernamePasswordAuthenticationToken  = (UsernamePasswordAuthenticationToken)this.tokenReader.getAuthentication(token);
        }
        return usernamePasswordAuthenticationToken;
    }
}
