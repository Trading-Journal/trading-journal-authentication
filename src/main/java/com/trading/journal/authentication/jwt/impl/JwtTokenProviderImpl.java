package com.trading.journal.authentication.jwt.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import com.trading.journal.authentication.jwt.DateHelper;
import com.trading.journal.authentication.jwt.JwtConstantsHelper;
import com.trading.journal.authentication.jwt.JwtTokenProvider;
import com.trading.journal.authentication.jwt.TokenData;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.Authority;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenProviderImpl implements JwtTokenProvider {

    private final SecretKey secretKey;

    public JwtTokenProviderImpl() {
        this.secretKey = Keys.hmacShaKeyFor(
                "1_sEQNtLZ33v4Ynye4tQ8pJ8lOhmjMNEs7XI-nJ0s6lKjyTMHmK7Gpfnz1xQmoF6zSlQMe4t34wua-YHeX4aCj3W5q9Ty3MPP7I1ULC3B9InNq8Y4_SpwciizpH7wsUlfEO1VAtV6MxSXhBaoYY1yI4UWRYvtAMH_idWiIA-y25x1KBF5slm9ry6DZa5t0mFpXzqFXjsrcxF724B_zKl--Ka-yG_jDdD-iPxyr8EWOIZgs2TVkgAn_jZ3-1VvH-HPvtCBrDdbVAc4NVK-o04Uyf2y-Fb72naYQbfFLkMk9_NCIpG6TpGeEGQR9e5wO0A87mzEGtTHDAV85WE5uXDw"
                        .getBytes());
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
                .setHeaderParam(JwtConstantsHelper.HEADER_TYP, JwtConstantsHelper.TOKEN_TYPE)
                .setIssuer(JwtConstantsHelper.TOKEN_ISSUER)
                .setAudience(JwtConstantsHelper.TOKEN_AUDIENCE)
                .setSubject(applicationUser.userName())
                .setIssuedAt(issuedAt)
                .setExpiration(getExpirationDate())
                .claim(JwtConstantsHelper.AUTHORITIES, authorities)
                .compact();
        return new TokenData(token, 3600, issuedAt);
    }

    @Override
    public Authentication getAuthentication(String token) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean validateToken(String token) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> getRoles(String token) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    private Date getExpirationDate() {
        return Date.from(LocalDateTime.now().plusSeconds(3600)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
