package com.trading.journal.authentication.jwt.impl;

import com.trading.journal.authentication.jwt.JwtConstantsHelper;

import org.springframework.security.authentication.AuthenticationServiceException;

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
        return bearerHeader.replace(JwtConstantsHelper.TOKEN_PREFIX, "");
    }
}
