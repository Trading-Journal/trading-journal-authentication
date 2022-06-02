package com.trading.journal.authentication.jwt.service.impl;

import com.trading.journal.authentication.jwt.service.JwtTokenReader;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfigArgumentResolvers implements WebFluxConfigurer {

    private final JwtTokenReader tokenReader;

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new AccessTokenResolver(this.tokenReader));
    }
}
