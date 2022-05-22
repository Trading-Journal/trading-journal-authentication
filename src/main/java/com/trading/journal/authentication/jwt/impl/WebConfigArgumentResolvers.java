package com.trading.journal.authentication.jwt.impl;

import com.trading.journal.authentication.jwt.JwtTokenParser;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
public class WebConfigArgumentResolvers implements WebFluxConfigurer {

    private final JwtTokenParser tokenParser;

    public WebConfigArgumentResolvers(JwtTokenParser tokenParser) {
        this.tokenParser = tokenParser;
    }

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new AccessTokenResolver(this.tokenParser));
    }
}
