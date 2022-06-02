package com.trading.journal.authentication.verification.properties;

import com.trading.journal.authentication.configuration.properties.HostProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@EnableConfigurationProperties({VerificationProperties.class, HostProperties.class})
@PropertySource(value = "application-verification-enabled.properties")
@ExtendWith(SpringExtension.class)
class VerificationPropertiesEnabledTest {
    @Autowired
    VerificationProperties properties;

    @Autowired
    HostProperties hostProperties;

    @DisplayName("VerificationProperties is enabled")
    @Test
    void enabled() {
        assertThat(properties.isEnabled()).isEqualTo(true);
    }

    @DisplayName("Host properties have back end and front end")
    @Test
    void host() {
        assertThat(hostProperties.getFrontEnd()).isEqualTo("http://site.com:8081");
        assertThat(hostProperties.getVerificationPage()).isEqualTo("auth/email-verified");
    }
}