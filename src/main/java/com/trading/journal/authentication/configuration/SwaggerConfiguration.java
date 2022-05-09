package com.trading.journal.authentication.configuration;

import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Response;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfiguration implements WebFluxConfigurer {
    private static final List<Response> GLOBAL_RESPONSES = Arrays.asList(
            new ResponseBuilder().code("403")
                    .description("Access forbidden to the resource").build(),
            new ResponseBuilder().code("404")
                    .description("Resource not found").build(),
            new ResponseBuilder().code("500")
                    .description("Server undefined exception").build());

    private static final String API_BASE_PACKAGE = "com.trading.journal.authentication.api";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(API_BASE_PACKAGE))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .pathMapping("/")
                .securityContexts(singletonList(securityContext()))
                .securitySchemes(singletonList(apiKey()))
                .useDefaultResponseMessages(false)
                .globalResponses(HttpMethod.GET, GLOBAL_RESPONSES)
                .globalResponses(HttpMethod.POST, GLOBAL_RESPONSES)
                .globalResponses(HttpMethod.PUT, GLOBAL_RESPONSES)
                .globalResponses(HttpMethod.PATCH, GLOBAL_RESPONSES)
                .globalResponses(HttpMethod.DELETE, GLOBAL_RESPONSES);
    }

    private ApiInfo apiInfo() {
        Contact contact = new Contact("Allan Weber", "https://allanweber.dev", "a.cassianoweber@gmail.com");
        return new ApiInfo(
                "Authentication for Trade Journal",
                "A HTTP REST API to authenticate to Trade Journal.",
                "1.0",
                "termsOfService",
                contact,
                "MIT", "", Collections.emptyList());
    }

    private ApiKey apiKey() {
        return new ApiKey("Bearer Token", AUTHORIZATION_HEADER, "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return singletonList(new SecurityReference("Bearer Token", authorizationScopes));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html**")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}