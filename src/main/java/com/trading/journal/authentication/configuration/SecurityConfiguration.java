package com.trading.journal.authentication.configuration;

import java.util.stream.Stream;

import com.trading.journal.authentication.jwt.JwtTokenAuthenticationFilter;
import com.trading.journal.authentication.jwt.JwtTokenReader;
import com.trading.journal.authentication.user.ApplicationUserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
public class SecurityConfiguration {

    private final ApplicationUserService applicationUserService;
    private final PasswordEncoder passwordEncoder;
    private final ServerAuthenticationExceptionEntryPoint serverAuthenticationExceptionEntryPoint;
    private final JwtTokenReader tokenReader;

    public SecurityConfiguration(ApplicationUserService applicationUserService, PasswordEncoder passwordEncoder,
            ServerAuthenticationExceptionEntryPoint serverAuthenticationExceptionEntryPoint,
            JwtTokenReader tokenReader) {
        this.applicationUserService = applicationUserService;
        this.passwordEncoder = passwordEncoder;
        this.serverAuthenticationExceptionEntryPoint = serverAuthenticationExceptionEntryPoint;
        this.tokenReader = tokenReader;
    }

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
                .exceptionHandling()
                .authenticationEntryPoint(serverAuthenticationExceptionEntryPoint)
                .and()
                .authorizeExchange(it -> it.pathMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .pathMatchers(getPublicPath())
                        .permitAll()
                        .pathMatchers(getAdminPath())
                        .hasAuthority(AuthoritiesHelper.ROLE_ADMIN)
                        .anyExchange()
                        .hasAuthority(AuthoritiesHelper.ROLE_USER))
                .addFilterAt(new JwtTokenAuthenticationFilter(tokenReader), SecurityWebFiltersOrder.HTTP_BASIC)
                .build();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(
                applicationUserService);
        authenticationManager.setPasswordEncoder(passwordEncoder);
        return authenticationManager;
    }

    private String[] getPublicPath() {
        String[] monitoring = { "/health/**", "/prometheus", "/metrics*/**" };
        String[] authentication = { "/authentication*/**" };
        String[] swagger = { "/", "/v2/api-docs", "/swagger*/**", "/webjars/**" };
        return Stream.of(monitoring, authentication, swagger).flatMap(Stream::of).toArray(String[]::new);
    }

    private String[] getAdminPath() {
        return new String[] { "/admin/**" };
    }
}
