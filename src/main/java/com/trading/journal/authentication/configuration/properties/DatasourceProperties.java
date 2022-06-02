package com.trading.journal.authentication.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ConfigurationProperties("journal.authentication.datasource")
@ConstructorBinding
public record DatasourceProperties(
        @NotBlank String host,
        @NotNull Integer port,
        @NotBlank String database,
        @NotBlank String username,
        @NotBlank String password
) {
}
