package com.trading.journal.authentication.user.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@EnableConfigurationProperties(AdminUserProperties.class)
@PropertySource(value = "application.properties")
@ExtendWith(SpringExtension.class)
class AdminUserPropertiesTest {

    @Autowired
    AdminUserProperties adminUserProperties;

    @DisplayName("Host properties have back end and front end")
    @Test
    void host() {
        assertThat(adminUserProperties.email()).isEqualTo("admin@email.com");
    }
}