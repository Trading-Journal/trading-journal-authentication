package com.trading.journal.authentication.configuration;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authentication.service.UserPasswordAuthenticationManager;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.jwt.JwtTokenAuthenticationFilter;
import com.trading.journal.authentication.jwt.service.JwtTokenReader;
import com.trading.journal.authentication.verification.service.impl.service.AuthorityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration {

    private final UserPasswordAuthenticationManager authenticationManager;
    private final ServerAuthenticationExceptionEntryPoint serverAuthenticationExceptionEntryPoint;
    private final JwtTokenReader tokenReader;
    private final AuthorityService authorityService;

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .exceptionHandling().authenticationEntryPoint(serverAuthenticationExceptionEntryPoint).and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(getPublicPath()).permitAll()
                .and()
                .addFilterBefore(new JwtTokenAuthenticationFilter(tokenReader), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        httpSecurity.cors();
        httpSecurity.csrf().disable();
        httpSecurity.headers().frameOptions().disable();

        Map<AuthorityCategory, String[]> authorityCategoryMap = getAuthorityCategoryMap();
        httpSecurity.authorizeRequests().antMatchers(getAdminPath()).hasAnyAuthority(authorityCategoryMap.get(AuthorityCategory.ADMINISTRATOR));
        httpSecurity.authorizeRequests().anyRequest().hasAnyAuthority(authorityCategoryMap.get(AuthorityCategory.COMMON_USER));
        return httpSecurity.authenticationManager(authenticationManager).build();
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
                    String[] authorities;
                    List<Authority> authoritiesByCategory = authorityService.getAuthoritiesByCategory(category);
                    if (authoritiesByCategory.isEmpty()) {
                        throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "No authorities found in the database, please load it");
                    } else {
                        authorities = authoritiesByCategory.stream()
                                .map(Authority::getName)
                                .toArray(String[]::new);
                    }
                    categoryAuthorities.put(category, authorities);
                });
        return categoryAuthorities;
    }
}
