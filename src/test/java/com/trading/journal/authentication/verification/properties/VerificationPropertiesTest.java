package com.trading.journal.authentication.verification.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@EnableConfigurationProperties(VerificationProperties.class)
@PropertySource(value = "application.properties")
@ExtendWith(SpringExtension.class)
class VerificationPropertiesTest {
    @Autowired
    VerificationProperties properties;

    @DisplayName("VerificationProperties are not enabled")
    @Test
    void enabledFalse() {
        assertThat(properties.enabled()).isEqualTo(false);
    }
}