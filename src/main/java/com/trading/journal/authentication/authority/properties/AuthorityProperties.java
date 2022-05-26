package com.trading.journal.authentication.authority.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("journal.authentication.authority")
@ConstructorBinding
public record AuthorityProperties(
        AuthorityPropertiesType type
) {
    public AuthorityProperties{
        if(type == null) {
            type = AuthorityPropertiesType.STATIC;
        }
    }
}
