package com.trading.journal.authentication.verification.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("journal.authentication.verification")
@ConstructorBinding
public record VerificationProperties(
        boolean enabled
) {
}
