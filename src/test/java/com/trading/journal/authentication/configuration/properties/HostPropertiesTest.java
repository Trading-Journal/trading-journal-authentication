package com.trading.journal.authentication.configuration.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@EnableConfigurationProperties(HostProperties.class)
@PropertySource(value = "application-verification-enabled.properties")
@ExtendWith(SpringExtension.class)
class HostPropertiesTest {

    @Autowired
    HostProperties hostProperties;

    @DisplayName("Host properties have back end and front end")
    @Test
    void host() {
        assertThat(hostProperties.getFrontEnd()).isEqualTo("http://site.com:8081");
        assertThat(hostProperties.getVerificationPage()).isEqualTo("email-verified");
        assertThat(hostProperties.getChangePasswordPage()).isEqualTo("change-password");
    }

}