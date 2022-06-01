package com.trading.journal.authentication.configuration.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("journal.authentication.hosts")
@Configuration
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HostProperties {

    private String backEnd;

    private String frontEnd;
}
