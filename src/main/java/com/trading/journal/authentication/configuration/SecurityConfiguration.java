package com.trading.journal.authentication.configuration;

import java.util.stream.Stream;

import com.trading.journal.authentication.user.ApplicationUserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
public class SecurityConfiguration {

        private static final String[] MONITORING_PUBLIC_PATH = { "/health/**", "/prometheus", "/metrics*/**" };
        private static final String[] AUTHENTICATION_PATH = { "/authentication*/**" };
        private static final String[] SWAGGER_PATH = { "/", "/v2/api-docs", "/swagger*/**", "/webjars/**" };
        private static final String[] PUBLIC_PATH = Stream.of(MONITORING_PUBLIC_PATH, AUTHENTICATION_PATH, SWAGGER_PATH)
                        .flatMap(Stream::of).toArray(String[]::new);

        private final ApplicationUserService applicationUserService;
        private final PasswordEncoder passwordEncoder;

        public SecurityConfiguration(ApplicationUserService applicationUserService, PasswordEncoder passwordEncoder) {
                this.applicationUserService = applicationUserService;
                this.passwordEncoder = passwordEncoder;
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

        @Bean
        public ReactiveAuthenticationManager reactiveAuthenticationManager() {
                UserDetailsRepositoryReactiveAuthenticationManager authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(
                                applicationUserService);
                authenticationManager.setPasswordEncoder(passwordEncoder);
                return authenticationManager;
        }
}
