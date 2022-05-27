package com.trading.journal.authentication.jwt.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import com.trading.journal.authentication.jwt.JwtException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JwtPropertiesTest {

    @DisplayName("Build JwtProperties for provider service type")
    @Test
    void provider() {
        JwtProperties properties = new JwtProperties(ServiceType.PROVIDER, new File("arg"), new File("arg"), 10L, 15L, "issuer", "audience");

        assertThat(properties.getServiceType()).isEqualTo(ServiceType.PROVIDER);
        assertThat(properties.getPrivateKey()).isNotNull();
        assertThat(properties.getPublicKey()).isNotNull();
        assertThat(properties.getAccessTokenExpiration()).isEqualTo(10L);
        assertThat(properties.getRefreshTokenExpiration()).isEqualTo(15L);
    }

    @DisplayName("Building JwtProperties for provider service type without private key throws exception")
    @Test
    void providerPrivateKeyException() {
        assertThrows(JwtException.class,
                () -> new JwtProperties(ServiceType.PROVIDER, null, new File("arg"), 10L, 15L, "issuer", "audience").init(),
                "For provider service type, both privateKey and publicKey must be provided");
    }

    @DisplayName("Building JwtProperties for provider service type without public key throws exception")
    @Test
    void providerPublicKeyException() {
        assertThrows(JwtException.class,
                () -> new JwtProperties(ServiceType.PROVIDER, new File("arg"), null, 10L, 15L, "issuer", "audience").init(),
                "For provider service type, both privateKey and publicKey must be provided");
    }

    @DisplayName("Building JwtProperties for provider service type without access token expiration throws exception")
    @Test
    void providerExpirationException() {
        assertThrows(JwtException.class,
                () -> new JwtProperties(ServiceType.PROVIDER, new File("arg"), new File("arg"), null, 15L, "issuer", "audience").init(),
                "For provider service type, access token and refresh token expiration must be provided");
    }

    @DisplayName("Building JwtProperties for provider service type without refresh token expiration throws exception")
    @Test
    void providerRefreshExpirationException() {
        assertThrows(JwtException.class,
                () -> new JwtProperties(ServiceType.PROVIDER, new File("arg"), new File("arg"), 15L, null, "issuer", "audience").init(),
                "For provider service type, access token and refresh token expiration must be provided");
    }

    @DisplayName("Build JwtProperties for resource service type")
    @Test
    void resource() {
        JwtProperties properties = new JwtProperties(ServiceType.RESOURCE, null, new File("arg"), null, null, "issuer", "audience");

        assertThat(properties.getServiceType()).isEqualTo(ServiceType.RESOURCE);
        assertThat(properties.getPrivateKey()).isNull();
        assertThat(properties.getPublicKey()).isNotNull();
        assertThat(properties.getAccessTokenExpiration()).isNull();

    }

    @DisplayName("Building JwtProperties for resource service type without public key throws exception")
    @Test
    void resourcePublicKeyException() {
        assertThrows(JwtException.class,
                () -> new JwtProperties(ServiceType.RESOURCE, null, null, null, null, "issuer", "audience").init(),
                "For resource service type, publicKey must be provided");
    }
}
