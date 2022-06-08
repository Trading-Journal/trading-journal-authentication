package com.trading.journal.authentication.jwt.service.impl;

import java.io.IOException;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.jwt.service.JwtTokenProvider;
import com.trading.journal.authentication.jwt.service.PrivateKeyProvider;
import com.trading.journal.authentication.jwt.data.JwtProperties;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.jwt.helper.DateHelper;
import com.trading.journal.authentication.jwt.helper.JwtConstants;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.userauthority.UserAuthority;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Slf4j
public class JwtTokenProviderImpl implements JwtTokenProvider {

    private final JwtProperties properties;
    private final Key privateKey;

    public JwtTokenProviderImpl(JwtProperties properties, PrivateKeyProvider privateKeyProvider) {
        this.properties = properties;
        try {
            this.privateKey = privateKeyProvider.provide(this.properties.getPrivateKey());
        } catch (IOException e) {
            log.error("Failed to load RSA private key", e);
            throw (ApplicationException) new ApplicationException(HttpStatus.UNAUTHORIZED,
                    "Failed to load RSA private key").initCause(e);
        }
    }

    @Override
    public TokenData generateAccessToken(ApplicationUser applicationUser) {
        List<String> authorities = Optional.ofNullable(applicationUser.getAuthorities())
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "User has no authorities"))
                .stream()
                .map(UserAuthority::getName)
                .collect(Collectors.toList());

        Date issuedAt = DateHelper.getUTCDatetimeAsDate();
        String accessToken = Jwts.builder()
                .signWith(this.privateKey, SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer(properties.getIssuer())
                .setAudience(properties.getAudience())
                .setSubject(applicationUser.getUserName())
                .setIssuedAt(issuedAt)
                .setExpiration(getExpirationDate(properties.getAccessTokenExpiration()))
                .claim(JwtConstants.SCOPES, authorities)
                .claim(JwtConstants.TENANCY, applicationUser.getUserName())
                .compact();

        return new TokenData(accessToken,
                issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    @Override
    public TokenData generateRefreshToken(ApplicationUser applicationUser) {
        Date issuedAt = DateHelper.getUTCDatetimeAsDate();
        String refreshToken = Jwts.builder()
                .signWith(this.privateKey, SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer(properties.getIssuer())
                .setAudience(properties.getAudience())
                .setSubject(applicationUser.getUserName())
                .setIssuedAt(issuedAt)
                .setExpiration(getExpirationDate(properties.getRefreshTokenExpiration()))
                .claim(JwtConstants.SCOPES, Collections.singletonList(JwtConstants.REFRESH_TOKEN))
                .compact();

        return new TokenData(refreshToken,
                issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    @Override
    public TokenData generateTemporaryToken(String email) {
        Date issuedAt = DateHelper.getUTCDatetimeAsDate();
        String refreshToken = Jwts.builder()
                .signWith(this.privateKey, SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer(properties.getIssuer())
                .setAudience(properties.getAudience())
                .setSubject(email)
                .setIssuedAt(issuedAt)
                .setExpiration(getExpirationDate(900L))
                .claim(JwtConstants.SCOPES, Collections.singletonList(JwtConstants.TEMPORARY_TOKEN))
                .compact();

        return new TokenData(refreshToken,
                issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    private Date getExpirationDate(Long expireIn) {
        return Date.from(LocalDateTime.now().plusSeconds(expireIn)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
