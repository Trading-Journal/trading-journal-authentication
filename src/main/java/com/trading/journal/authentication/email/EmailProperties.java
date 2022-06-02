package com.trading.journal.authentication.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties("spring.mail")
@Data
@Configuration
public class EmailProperties {

    @NotBlank
    private String username;
}
