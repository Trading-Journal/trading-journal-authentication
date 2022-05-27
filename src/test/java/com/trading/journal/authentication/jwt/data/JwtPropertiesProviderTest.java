package com.trading.journal.authentication.jwt.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(JwtProperties.class)
@PropertySource(value = "application-provider.properties")
@ExtendWith(SpringExtension.class)
public class JwtPropertiesProviderTest {

    @Autowired
    JwtProperties properties;

    @DisplayName("JwtProperties for provider service type")
    @Test
    void provider() {
        assertThat(properties.getServiceType()).isEqualTo(ServiceType.PROVIDER);
        assertThat(properties.getPrivateKey()).isNotNull();
        assertThat(properties.getPublicKey()).isNotNull();
        assertThat(properties.getAccessTokenExpiration()).isEqualTo(3600L);
    }
}
