package com.trading.journal.authentication.configuration;

import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
public class SecurityConfiguration {

    private static final String[] MONITORING_PUBLIC_PATH = { "/health/**", "/prometheus", "/metrics*/**" };
    private static final String[] AUTHENTICATION_PATH = { "/authentication*/**" };
    private static final String[] SWAGGER_PATH = { "/", "/v2/api-docs", "/swagger*/**", "/webjars/**" };
    private static final String[] PUBLIC_PATH = Stream.of(MONITORING_PUBLIC_PATH, AUTHENTICATION_PATH, SWAGGER_PATH)
            .flatMap(Stream::of).toArray(String[]::new);

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .cors()
                .and()
                .headers()
                .frameOptions()
                .disable()
                .and()
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                // .exceptionHandling()
                // .authenticationEntryPoint(authenticationExceptionEntryPoint)
                // .and()
                .authorizeExchange(it -> it.pathMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .pathMatchers(PUBLIC_PATH)
                        .permitAll())
                // .addFilterAt(new JwtTokenAuthenticationFilter(tokenProvider),
                // SecurityWebFiltersOrder.HTTP_BASIC)
                .build();
    }
}
