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
import com.trading.journal.authentication.jwt.JwtTokenProvider;
import com.trading.journal.authentication.jwt.PrivateKeyProvider;
import com.trading.journal.authentication.jwt.data.JwtProperties;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.jwt.helper.DateHelper;
import com.trading.journal.authentication.jwt.helper.JwtConstants;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.Authority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtTokenProviderImpl implements JwtTokenProvider {

    private final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final JwtProperties properties;
    private final PrivateKeyProvider privateKeyProvider;

    public JwtTokenProviderImpl(JwtProperties properties, PrivateKeyProvider privateKeyProvider) {
        this.properties = properties;
        this.privateKeyProvider = privateKeyProvider;
    }

    @Override
    public TokenData generateJwtToken(ApplicationUser applicationUser) {
        Key privateKey;
        try {
            privateKey = privateKeyProvider.provide(this.properties.privateKey());
        } catch (IOException e) {
            logger.error("Failed to load RSA private key", e);
            throw (ApplicationException) new ApplicationException(HttpStatus.UNAUTHORIZED,
                    "Failed to load RSA private key").initCause(e);
        }

        List<String> authorities = Optional.ofNullable(applicationUser.authorities())
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "User has not authority roles"))
                .stream()
                .map(Authority::name)
                .collect(Collectors.toList());

        Date issuedAt = DateHelper.getUTCDatetimeAsDate();
        String token = Jwts.builder()
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .setHeaderParam(JwtConstants.HEADER_TYP, JwtConstants.TOKEN_TYPE)
                .setIssuer(JwtConstants.TOKEN_ISSUER)
                .setAudience(JwtConstants.TOKEN_AUDIENCE)
                .setSubject(applicationUser.userName())
                .setIssuedAt(issuedAt)
                .setExpiration(getExpirationDate())
                .claim(JwtConstants.AUTHORITIES, authorities)
                .claim(JwtConstants.TENANCY, applicationUser.userName())
                .compact();
        return new TokenData(token, properties.expiration(), issuedAt);
    }

    private Date getExpirationDate() {
        return Date.from(LocalDateTime.now().plusSeconds(properties.expiration())
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
