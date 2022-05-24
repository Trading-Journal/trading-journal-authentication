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
@PropertySource(value = "application-resource.properties")
@ExtendWith(SpringExtension.class)
public class JwtPropertiesResourceTest {

    @Autowired
    JwtProperties properties;

    @DisplayName("JwtProperties for resource service type")
    @Test
    void provider() {
        assertThat(properties.serviceType()).isEqualTo(ServiceType.RESOURCE);
        assertThat(properties.privateKey()).isNull();
        assertThat(properties.publicKey()).isNotNull();
        assertThat(properties.accessTokenExpiration()).isNull();
    }
}
