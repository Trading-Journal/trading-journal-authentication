package com.trading.journal.authentication.jwt.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import com.trading.journal.authentication.jwt.AccessTokenInfo;
import com.trading.journal.authentication.jwt.DateHelper;
import com.trading.journal.authentication.jwt.JwtHelper;
import com.trading.journal.authentication.jwt.JwtTokenProvider;
import com.trading.journal.authentication.jwt.TokenData;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.Authority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenProviderImpl implements JwtTokenProvider {

    private final Logger logger = LoggerFactory.getLogger(JwtTokenProviderImpl.class);
    private static final String TOKEN_SECRET = "1_sEQNtLZ33v4Ynye4tQ8pJ8lOhmjMNEs7XI-nJ0s6lKjyTMHmK7Gpfnz1xQmoF6zSlQMe4t34wua-YHeX4aCj3W5q9Ty3MPP7I1ULC3B9InNq8Y4_SpwciizpH7wsUlfEO1VAtV6MxSXhBaoYY1yI4UWRYvtAMH_idWiIA-y25x1KBF5slm9ry6DZa5t0mFpXzqFXjsrcxF724B_zKl--Ka-yG_jDdD-iPxyr8EWOIZgs2TVkgAn_jZ3-1VvH-HPvtCBrDdbVAc4NVK-o04Uyf2y-Fb72naYQbfFLkMk9_NCIpG6TpGeEGQR9e5wO0A87mzEGtTHDAV85WE5uXDw";
    private final SecretKey secretKey;
    private final JwtTokenParser tokenParser;

    public JwtTokenProviderImpl() {
        this.secretKey = Keys.hmacShaKeyFor(TOKEN_SECRET.getBytes());
        this.tokenParser = new JwtTokenParser(TOKEN_SECRET);
    }

    @Override
    public TokenData generateJwtToken(ApplicationUser applicationUser) {
        Date issuedAt = DateHelper.getUTCDatetimeAsDate();
        List<String> authorities = Optional.ofNullable(applicationUser.authorities())
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new AuthenticationServiceException("User has not authority roles"))
                .stream()
                .map(Authority::name)
                .collect(Collectors.toList());

        String token = Jwts.builder()
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .setHeaderParam(JwtHelper.HEADER_TYP, JwtHelper.TOKEN_TYPE)
                .setIssuer(JwtHelper.TOKEN_ISSUER)
                .setAudience(JwtHelper.TOKEN_AUDIENCE)
                .setSubject(applicationUser.userName())
                .setIssuedAt(issuedAt)
                .setExpiration(getExpirationDate())
                .claim(JwtHelper.AUTHORITIES, authorities)
                .claim(JwtHelper.TENANCY, applicationUser.userName())
                .compact();
        return new TokenData(token, 3600, issuedAt);
    }

    @Override
    public boolean validateToken(String token) {
        boolean isValid = false;
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            isValid = !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            logger.info("Invalid JWT token.");
            logger.trace("Invalid JWT token trace.", e);
        }
        return isValid;
    }

    @Override
    public Authentication getAuthentication(String token) {
        return tokenParser.getAuthentication(token);
    }

    @Override
    public List<String> getRoles(String token) {
        return tokenParser.getRoles(token);
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

    @Override
    public AccessTokenInfo getAccessTokenInfo(String token) {
        return tokenParser.getAccessTokenInfo(token);
    }

    private Date getExpirationDate() {
        return Date.from(LocalDateTime.now().plusSeconds(3600)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
