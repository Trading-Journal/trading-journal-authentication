package com.trading.journal.authentication.jwt.data;

import java.io.File;

import com.trading.journal.authentication.jwt.JwtException;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("app.jwt")
@ConstructorBinding
public record JwtProperties(
        ServiceType serviceType,
        File privateKey,
        File publicKey,
        Long accessTokenExpiration,
        Long refreshTokenExpiration) {

    public JwtProperties {
        if (ServiceType.PROVIDER.equals(serviceType)) {
            if (privateKey == null || publicKey == null) {
                throw new JwtException("For provider service type, both privateKey and publicKey must be provided");
            }
            if (accessTokenExpiration == null || refreshTokenExpiration == null) {
                throw new JwtException(
                        "For provider service type, access token and refresh token expiration must be provided");
            }
        } else if (publicKey == null) {
            throw new JwtException("For resource service type, publicKey must be provided");
        }
    }
}
