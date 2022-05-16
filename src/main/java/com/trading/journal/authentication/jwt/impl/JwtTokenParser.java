package com.trading.journal.authentication.jwt.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.trading.journal.authentication.jwt.AccessTokenInfo;
import com.trading.journal.authentication.jwt.ContextUser;
import com.trading.journal.authentication.jwt.JwtHelper;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

public class JwtTokenParser {

    private final String secret;

    public JwtTokenParser(String secret) {
        this.secret = secret;
    }

    public Authentication getAuthentication(String token) {
        var jwsClaims = parseToken(token);
        Collection<? extends GrantedAuthority> authorities = getAuthorities(jwsClaims);
        String tenancy = getTenancy(jwsClaims);
        ContextUser principal = new ContextUser(jwsClaims.getBody().getSubject(), authorities, tenancy);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public List<String> getRoles(String token) {
        var jwsClaims = parseToken(token);
        return getAuthorities(jwsClaims).stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public AccessTokenInfo getAccessTokenInfo(String token) {
        var jwsClaims = parseToken(token);
        List<String> authorities = getAuthorities(jwsClaims).stream().map(a -> a.getAuthority())
                .collect(Collectors.toList());
        String tenancy = getTenancy(jwsClaims);
        return new AccessTokenInfo(jwsClaims.getBody().getSubject(), tenancy, authorities);
    }

    private List<SimpleGrantedAuthority> getAuthorities(Jws<Claims> token) {
        return ((List<?>) token.getBody().get(JwtHelper.AUTHORITIES))
                .stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .collect(Collectors.toList());
    }

    private String getTenancy(Jws<Claims> token) {
        return Optional.ofNullable(token.getBody().get(JwtHelper.TENANCY)).map(Object::toString)
                .orElseThrow(() -> new AuthenticationServiceException(
                        String.format("User company not found inside the token %s", token)));
    }

    @SuppressWarnings("PMD")
    public Jws<Claims> parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes())
                    .build()
                    .parseClaimsJws(resolveToken(token));
        } catch (ExpiredJwtException exception) {
            throw new AuthenticationServiceException(
                    String.format("Request to parse expired JWT : %s failed : %s", token, exception.getMessage()));
        } catch (UnsupportedJwtException exception) {
            throw new AuthenticationServiceException(
                    String.format("Request to parse unsupported JWT : %s failed : %s", token, exception.getMessage()));
        } catch (MalformedJwtException exception) {
            throw new AuthenticationServiceException(
                    String.format("Request to parse invalid JWT : %s failed : %s", token, exception.getMessage()));
        } catch (SignatureException exception) {
            throw new AuthenticationServiceException(String.format(
                    "Request to parse JWT with invalid signature : %s failed : %s", token, exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            throw new AuthenticationServiceException(String
                    .format("Request to parse empty or null JWT : %s failed : %s", token, exception.getMessage()));
        }
    }

    private String resolveToken(String bearerHeader) {
        return bearerHeader.replace(JwtHelper.TOKEN_PREFIX, "");
    }
}
