package com.trading.journal.authentication.jwt.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.jwt.JwtTokenParser;
import com.trading.journal.authentication.jwt.JwtTokenReader;
import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.jwt.data.ContextUser;
import com.trading.journal.authentication.jwt.helper.JwtConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

@Service
public class JwtTokenReaderImpl implements JwtTokenReader {

    private final Logger logger = LoggerFactory.getLogger(JwtTokenReader.class);

    private final JwtTokenParser tokenParser;

    public JwtTokenReaderImpl(JwtTokenParser tokenParser) {
        this.tokenParser = tokenParser;
    }

    @Override
    public Authentication getAuthentication(String token) {
        Jws<Claims> jwsClaims = tokenParser.parseToken(token);
        Collection<? extends GrantedAuthority> authorities = getAuthorities(jwsClaims);
        String tenancy = getTenancy(jwsClaims);
        ContextUser principal = new ContextUser(jwsClaims.getBody().getSubject(), authorities, tenancy);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    @Override
    public AccessTokenInfo getAccessTokenInfo(String token) {
        Jws<Claims> jwsClaims = tokenParser.parseToken(token);
        List<String> authorities = getAuthorities(jwsClaims).stream().map(a -> a.getAuthority())
                .collect(Collectors.toList());
        String tenancy = getTenancy(jwsClaims);
        return new AccessTokenInfo(jwsClaims.getBody().getSubject(), tenancy, authorities);
    }

    @Override
    public boolean isTokenValid(String token) {
        boolean isValid = false;
        try {
            Jws<Claims> claims = tokenParser.parseToken(token);
            isValid = !claims.getBody().getExpiration().before(new Date());
        } catch (ApplicationException e) {
            logger.info("Invalid JWT token.");
            logger.trace("Invalid JWT token trace.", e);
        }
        return isValid;
    }

    private List<SimpleGrantedAuthority> getAuthorities(Jws<Claims> token) {
        return ((List<?>) token.getBody().get(JwtConstants.AUTHORITIES))
                .stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .collect(Collectors.toList());
    }

    private String getTenancy(Jws<Claims> token) {
        return Optional.ofNullable(token.getBody().get(JwtConstants.TENANCY)).map(Object::toString)
                .orElseThrow(() -> new AuthenticationServiceException(
                        String.format("User tenancy not found inside the token %s", token)));
    }

}
