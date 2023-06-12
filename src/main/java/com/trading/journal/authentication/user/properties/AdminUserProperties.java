package com.trading.journal.authentication.user.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("journal.authentication.admin-user")
public record AdminUserProperties(
        @NotBlank String email
) {
}
