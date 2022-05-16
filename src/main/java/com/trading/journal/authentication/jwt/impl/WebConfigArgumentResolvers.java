package com.trading.journal.authentication.jwt.impl;

import com.trading.journal.authentication.jwt.JwtTokenProvider;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
public class WebConfigArgumentResolvers implements WebFluxConfigurer {

    private final JwtTokenProvider tokenProvider;

    public WebConfigArgumentResolvers(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new AccessTokenResolver(tokenProvider));
    }
}
