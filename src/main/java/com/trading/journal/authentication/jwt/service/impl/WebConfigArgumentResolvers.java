package com.trading.journal.authentication.jwt.service.impl;

import com.trading.journal.authentication.jwt.service.JwtTokenReader;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfigArgumentResolvers implements WebMvcConfigurer {

    private final JwtTokenReader tokenReader;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new AccessTokenResolver(tokenReader));
    }
}
