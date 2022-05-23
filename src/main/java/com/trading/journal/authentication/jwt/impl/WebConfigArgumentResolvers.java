package com.trading.journal.authentication.jwt.impl;

import com.trading.journal.authentication.jwt.JwtTokenReader;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
public class WebConfigArgumentResolvers implements WebFluxConfigurer {

    private final JwtTokenReader tokenReader;

    public WebConfigArgumentResolvers(JwtTokenReader tokenReader) {
        this.tokenReader = tokenReader;
    }

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new AccessTokenResolver(this.tokenReader));
    }
}
