package com.trading.journal.authentication.jwt.data;

import com.trading.journal.authentication.jwt.JwtException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.File;

@ConfigurationProperties("application.authentication.jwt")
@Configuration
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtProperties {
    @NotNull
    private ServiceType serviceType;

    private File privateKey;

    @NotNull
    private File publicKey;

    private Long accessTokenExpiration;

    private Long refreshTokenExpiration;

    @NotNull
    private String issuer;

    @NotNull
    private String audience;

    @PostConstruct
    public void init() {
        if (ServiceType.PROVIDER.equals(serviceType)) {
            validateProvider();
        } else if (publicKey == null) {
            throw new JwtException("For resource service type, publicKey must be provided");
        }
    }

    private void validateProvider() {
        if (privateKey == null || publicKey == null) {
            throw new JwtException("For provider service type, both privateKey and publicKey must be provided");
        } else if (accessTokenExpiration == null || refreshTokenExpiration == null) {
            throw new JwtException("For provider service type, access token and refresh token expiration must be provided");
        }
    }
}
