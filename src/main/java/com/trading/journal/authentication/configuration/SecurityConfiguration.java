package com.trading.journal.authentication.configuration;

import com.allanweber.jwttoken.service.JwtTokenAuthenticationCheck;
import com.trading.journal.authentication.authentication.service.UserPasswordAuthenticationManager;
import com.trading.journal.authentication.authority.AuthorityCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration {

    private final UserPasswordAuthenticationManager authenticationManager;
    private final ServerAuthenticationExceptionEntryPoint serverAuthenticationExceptionEntryPoint;
    private final JwtTokenAuthenticationCheck jwtTokenAuthenticationCheck;

    private final LoadAuthorities loadAuthorities;

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .exceptionHandling().authenticationEntryPoint(serverAuthenticationExceptionEntryPoint).and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(getPublicPath()).permitAll()
                .and()
                .addFilterBefore(new JwtTokenAuthenticationFilter(jwtTokenAuthenticationCheck), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        httpSecurity.cors().configurationSource(configureCors());
        httpSecurity.csrf().disable();
        httpSecurity.headers().frameOptions().disable();

        Map<AuthorityCategory, String[]> authorityCategoryMap = loadAuthorities.getAuthorityCategoryMap();
        httpSecurity.authorizeRequests().antMatchers(getAdminPath()).hasAnyAuthority(authorityCategoryMap.get(AuthorityCategory.ADMINISTRATOR));
        httpSecurity.authorizeRequests().antMatchers(getOrganisationAdminPath()).hasAnyAuthority(authorityCategoryMap.get(AuthorityCategory.ORGANISATION));
        httpSecurity.authorizeRequests().anyRequest().hasAnyAuthority(authorityCategoryMap.get(AuthorityCategory.COMMON_USER));
        return httpSecurity.authenticationManager(authenticationManager).build();
    }

    private CorsConfigurationSource configureCors() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(asList("http://localhost:3000/", "https://*.tradefastapp.com/"));
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod(HttpMethod.GET);
        configuration.addAllowedMethod(HttpMethod.POST);
        configuration.addAllowedMethod(HttpMethod.PATCH);
        configuration.addAllowedMethod(HttpMethod.PUT);
        configuration.addAllowedMethod(HttpMethod.DELETE);
        configuration.addAllowedMethod(HttpMethod.OPTIONS);
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        return request -> configuration;
    }

    private String[] getPublicPath() {
        String[] monitoring = {"/health/**", "/prometheus", "/metrics*/**"};
        String[] authentication = {"/auth/**"};
        String[] swagger = {"/", "/v2/api-docs", "/swagger*/**", "/webjars/**"};
        return Stream.of(monitoring, authentication, swagger).flatMap(Stream::of).toArray(String[]::new);
    }

    private String[] getAdminPath() {
        return new String[]{"/admin/**"};
    }

    private String[] getOrganisationAdminPath() {
        return new String[]{"/organisation/**"};
    }
}
