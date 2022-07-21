package com.trading.journal.authentication.jwt.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.jwt.data.ContextUser;
import com.trading.journal.authentication.jwt.data.JwtProperties;
import com.trading.journal.authentication.jwt.helper.JwtConstants;
import com.trading.journal.authentication.jwt.service.JwtTokenParser;
import com.trading.journal.authentication.jwt.service.JwtTokenReader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenReaderImpl implements JwtTokenReader {

    private final JwtTokenParser tokenParser;

    private final JwtProperties properties;

    @Override
    public Authentication getAuthentication(String token) {
        Jws<Claims> jwsClaims = tokenParser.parseToken(token);
        Collection<? extends GrantedAuthority> authorities = getAuthorities(jwsClaims);
        ContextUser principal = new ContextUser(jwsClaims.getBody().getSubject(), getTenancyId(jwsClaims), getTenancyName(jwsClaims));
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    @Override
    public AccessTokenInfo getAccessTokenInfo(String token) {
        Jws<Claims> jwsClaims = tokenParser.parseToken(token);
        List<String> authorities = getAuthorities(jwsClaims).stream().map(SimpleGrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return new AccessTokenInfo(jwsClaims.getBody().getSubject(), getTenancyId(jwsClaims), getTenancyName(jwsClaims), authorities);
    }

    @Override
    public AccessTokenInfo getTokenInfo(String token) {
        Jws<Claims> jwsClaims = tokenParser.parseToken(token);
        List<String> authorities = getAuthorities(jwsClaims).stream().map(SimpleGrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return new AccessTokenInfo(jwsClaims.getBody().getSubject(), null, null, authorities);
    }

    @Override
    public boolean isTokenValid(String token) {
        boolean isValid = false;
        try {
            Jws<Claims> claims = tokenParser.parseToken(token);
            boolean sameIssuer = properties.getIssuer().equals(claims.getBody().getIssuer());
            boolean sameAudience = properties.getAudience().equals(claims.getBody().getAudience());
            isValid = sameIssuer && sameAudience;
        } catch (ApplicationException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace.", e);
        }
        return isValid;
    }

    private List<SimpleGrantedAuthority> getAuthorities(Jws<Claims> token) {
        return ((List<?>) token.getBody().get(JwtConstants.SCOPES))
                .stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .collect(Collectors.toList());
    }

    private Long getTenancyId(Jws<Claims> token) {
        return Optional.ofNullable(token.getBody().get(JwtConstants.TENANCY_ID))
                .map(Object::toString)
                .map(Long::parseLong)
                .orElse(null);
    }

    private String getTenancyName(Jws<Claims> token) {
        return Optional.ofNullable(token.getBody().get(JwtConstants.TENANCY_NAME))
                .map(Object::toString)
                .orElse(null);
    }
}
