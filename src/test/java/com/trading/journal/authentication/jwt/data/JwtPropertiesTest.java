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
        JwtProperties properties = new JwtProperties(ServiceType.PROVIDER, new File("arg"), new File("arg"), 10L, 15L);

        assertThat(properties.serviceType()).isEqualTo(ServiceType.PROVIDER);
        assertThat(properties.privateKey()).isNotNull();
        assertThat(properties.publicKey()).isNotNull();
        assertThat(properties.accessTokenExpiration()).isEqualTo(10L);
        assertThat(properties.refreshTokenExpiration()).isEqualTo(15L);
    }

    @DisplayName("Building JwtProperties for provider service type without private key throws exception")
    @Test
    void providerPrivateKeyException() {
        assertThrows(JwtException.class,
                () -> new JwtProperties(ServiceType.PROVIDER, null, new File("arg"), 10L, 15L),
                "For provider service type, both privateKey and publicKey must be provided");
    }

    @DisplayName("Building JwtProperties for provider service type without public key throws exception")
    @Test
    void providerPublicKeyException() {
        assertThrows(JwtException.class,
                () -> new JwtProperties(ServiceType.PROVIDER, new File("arg"), null, 10L, 15L),
                "For provider service type, both privateKey and publicKey must be provided");
    }

    @DisplayName("Building JwtProperties for provider service type without access token expiration throws exception")
    @Test
    void providerExpirationException() {
        assertThrows(JwtException.class,
                () -> new JwtProperties(ServiceType.PROVIDER, new File("arg"), new File("arg"), null, 15L),
                "For provider service type, access token and refresh token expiration must be provided");
    }

    @DisplayName("Building JwtProperties for provider service type without refresh token expiration throws exception")
    @Test
    void providerRefreshExpirationException() {
        assertThrows(JwtException.class,
                () -> new JwtProperties(ServiceType.PROVIDER, new File("arg"), new File("arg"), 15L, null),
                "For provider service type, access token and refresh token expiration must be provided");
    }

    @DisplayName("Build JwtProperties for resource service type")
    @Test
    void resource() {
        JwtProperties properties = new JwtProperties(ServiceType.RESOURCE, null, new File("arg"), null, null);

        assertThat(properties.serviceType()).isEqualTo(ServiceType.RESOURCE);
        assertThat(properties.privateKey()).isNull();
        assertThat(properties.publicKey()).isNotNull();
        assertThat(properties.accessTokenExpiration()).isNull();

    }

    @DisplayName("Building JwtProperties for resource service type without public key throws exception")
    @Test
    void resourcePublicKeyException() {
        assertThrows(JwtException.class,
                () -> new JwtProperties(ServiceType.RESOURCE, null, null, null, null),
                "For resource service type, publicKey must be provided");
    }
}
