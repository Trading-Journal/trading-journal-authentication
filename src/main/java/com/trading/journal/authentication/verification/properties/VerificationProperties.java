package com.trading.journal.authentication.verification.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("journal.authentication.verification")
@Configuration
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationProperties {

    private boolean enabled;

    public boolean isDisabled(){
        return !this.enabled;
    }
}
