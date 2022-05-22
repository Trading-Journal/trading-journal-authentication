package com.trading.journal.authentication.jwt.impl;

import java.io.IOException;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.jwt.JwtException;
import com.trading.journal.authentication.jwt.JwtTokenProvider;
import com.trading.journal.authentication.jwt.PrivateKeyProvider;
import com.trading.journal.authentication.jwt.data.JwtProperties;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.jwt.helper.DateHelper;
import com.trading.journal.authentication.jwt.helper.JwtHelper;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.Authority;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtTokenProviderImpl implements JwtTokenProvider {

    private final JwtProperties properties;

    private final Key privateKey;

    public JwtTokenProviderImpl(JwtProperties properties, PrivateKeyProvider privateKeyProvider) {
        this.properties = properties;
        try {
            this.privateKey = privateKeyProvider.provide(this.properties.getPrivateKey());
        } catch (IOException e) {
            throw new JwtException("Failed to load RSA private key", e);
        }
    }

    @Override
    public TokenData generateJwtToken(ApplicationUser applicationUser) {
        Date issuedAt = DateHelper.getUTCDatetimeAsDate();
        List<String> authorities = Optional.ofNullable(applicationUser.authorities())
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "User has not authority roles"))
                .stream()
                .map(Authority::name)
                .collect(Collectors.toList());

        String token = Jwts.builder()
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .setHeaderParam(JwtHelper.HEADER_TYP, JwtHelper.TOKEN_TYPE)
                .setIssuer(JwtHelper.TOKEN_ISSUER)
                .setAudience(JwtHelper.TOKEN_AUDIENCE)
                .setSubject(applicationUser.userName())
                .setIssuedAt(issuedAt)
                .setExpiration(getExpirationDate())
                .claim(JwtHelper.AUTHORITIES, authorities)
                .claim(JwtHelper.TENANCY, applicationUser.userName())
                .compact();
        return new TokenData(token, properties.getExpiration(), issuedAt);
    }

    private Date getExpirationDate() {
        return Date.from(LocalDateTime.now().plusSeconds(properties.getExpiration())
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
