package com.trading.journal.authentication.user.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties("journal.authentication.admin-user")
@ConstructorBinding
public record AdminUserProperties(
        @NotBlank String email
) {
}
