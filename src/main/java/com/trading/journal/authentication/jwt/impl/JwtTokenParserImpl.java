package com.trading.journal.authentication.jwt.impl;

import java.io.IOException;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.jwt.JwtException;
import com.trading.journal.authentication.jwt.JwtTokenParser;
import com.trading.journal.authentication.jwt.PublicKeyProvider;
import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.jwt.data.ContextUser;
import com.trading.journal.authentication.jwt.data.JwtProperties;
import com.trading.journal.authentication.jwt.helper.JwtHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtTokenParserImpl implements JwtTokenParser {

    SPLIT INTO
    TOKEN PARSER
    AND TOKEN READER

    private final Logger logger = LoggerFactory.getLogger(JwtTokenParserImpl.class);

    private final Key publicKey;

    public JwtTokenParserImpl(PublicKeyProvider publicKeyProvider, JwtProperties properties) {
        try {
            this.publicKey = publicKeyProvider.provide(properties.getPublicKey());
        } catch (IOException e) {
            throw (JwtException) new JwtException(e.getMessage()).initCause(e);
        }
    }

    @Override
    public Authentication getAuthentication(String token) {
        var jwsClaims = parseToken(token);
        Collection<? extends GrantedAuthority> authorities = getAuthorities(jwsClaims);
        String tenancy = getTenancy(jwsClaims);
        ContextUser principal = new ContextUser(jwsClaims.getBody().getSubject(), authorities, tenancy);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    @Override
    public AccessTokenInfo getAccessTokenInfo(String token) {
        var jwsClaims = parseToken(token);
        List<String> authorities = getAuthorities(jwsClaims).stream().map(a -> a.getAuthority())
                .collect(Collectors.toList());
        String tenancy = getTenancy(jwsClaims);
        return new AccessTokenInfo(jwsClaims.getBody().getSubject(), tenancy, authorities);
    }

    @Override
    public boolean isTokenValid(String token) {
        boolean isValid = false;
        try {
            Jws<Claims> claims = parseToken(token);
            isValid = !claims.getBody().getExpiration().before(new Date());
        } catch (ApplicationException e) {
            logger.info("Invalid JWT token.");
            logger.trace("Invalid JWT token trace.", e);
        }
        return isValid;
    }

    @Override
    public String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtHelper.TOKEN_PREFIX)) {
            token = bearerToken.replace(JwtHelper.TOKEN_PREFIX, "");
        }
        return token;
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
                    .setSigningKey(this.publicKey)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException exception) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED,
                    String.format("Request to parse expired JWT : %s failed : %s", token, exception.getMessage()));
        } catch (UnsupportedJwtException exception) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED,
                    String.format("Request to parse unsupported JWT : %s failed : %s", token, exception.getMessage()));
        } catch (MalformedJwtException exception) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED,
                    String.format("Request to parse invalid JWT : %s failed : %s", token, exception.getMessage()));
        } catch (SignatureException exception) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED,
                    String.format("Request to parse JWT with invalid signature : %s failed : %s", token,
                            exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED,
                    String.format("Request to parse empty or null JWT : %s failed : %s", token,
                            exception.getMessage()));
        }
    }
}
