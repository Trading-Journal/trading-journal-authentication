package com.trading.journal.authentication.configuration;

import com.trading.journal.authentication.authority.AuthoritiesHelper;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.service.AuthorityService;
import com.trading.journal.authentication.jwt.JwtTokenAuthenticationFilter;
import com.trading.journal.authentication.jwt.service.JwtTokenReader;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration {

    private final ApplicationUserService applicationUserService;
    private final PasswordEncoder passwordEncoder;
    private final ServerAuthenticationExceptionEntryPoint serverAuthenticationExceptionEntryPoint;
    private final JwtTokenReader tokenReader;

    private final AuthorityService authorityService;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        ServerHttpSecurity serverHttpSecurity = http.csrf(ServerHttpSecurity.CsrfSpec::disable)
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
                .authorizeExchange(exchangeSpec -> exchangeSpec.pathMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .pathMatchers(getPublicPath())
                        .permitAll());

        Map<AuthorityCategory, String[]> authorityCategoryMap = getAuthorityCategoryMap();
        serverHttpSecurity.authorizeExchange(exchangeSpec -> exchangeSpec.pathMatchers(getAdminPath()).hasAnyAuthority(authorityCategoryMap.get(AuthorityCategory.ADMINISTRATOR)));
        serverHttpSecurity.authorizeExchange(exchangeSpec -> exchangeSpec.anyExchange().hasAnyAuthority(authorityCategoryMap.get(AuthorityCategory.COMMON_USER)));

        return serverHttpSecurity.addFilterAt(new JwtTokenAuthenticationFilter(tokenReader), SecurityWebFiltersOrder.HTTP_BASIC).build();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(
                applicationUserService);
        authenticationManager.setPasswordEncoder(passwordEncoder);
        return authenticationManager;
    }

    private String[] getPublicPath() {
        String[] monitoring = {"/health/**", "/prometheus", "/metrics*/**"};
        String[] authentication = {"/authentication*/**"};
        String[] swagger = {"/", "/v2/api-docs", "/swagger*/**", "/webjars/**"};
        return Stream.of(monitoring, authentication, swagger).flatMap(Stream::of).toArray(String[]::new);
    }

    private String[] getAdminPath() {
        return new String[]{"/admin/**"};
    }

    private Map<AuthorityCategory, String[]> getAuthorityCategoryMap() {
        Map<AuthorityCategory, String[]> categoryAuthorities = new ConcurrentHashMap<>();
        Arrays.stream(AuthorityCategory.values()).toList()
                .forEach(category -> {
                    String[] authorities = authorityService.getAuthoritiesByCategory(category).collectList().blockOptional()
                            .filter(list -> !list.isEmpty())
                            .orElseGet(() -> {
                                log.info("No authorities found in the database for {} category, so it will use the default authorities.", category);
                                return AuthoritiesHelper.getByCategory(category)
                                        .stream()
                                        .map(authoritiesHelper -> new Authority(authoritiesHelper.getCategory(), authoritiesHelper.getLabel()))
                                        .collect(Collectors.toList());
                            })
                            .stream()
                            .map(Authority::getName)
                            .toArray(String[]::new);
                    categoryAuthorities.put(category, authorities);
                });
        return categoryAuthorities;
    }
}
