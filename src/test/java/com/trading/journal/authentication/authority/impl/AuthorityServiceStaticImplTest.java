package com.trading.journal.authentication.authority.impl;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.service.impl.AuthorityServiceStaticImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorityServiceStaticImplTest {

    @DisplayName("Given AuthorityCategory COMMON_USER load only one Authority")
    @Test
    void loadOneCommonUserAuthority() {
        Flux<Authority> flux = new AuthorityServiceStaticImpl().getAuthoritiesByCategory(AuthorityCategory.COMMON_USER);

        StepVerifier.create(flux)
                .assertNext(authority -> {
                    assertThat(authority.getId()).isNull();
                    assertThat(authority.getName()).isEqualTo("ROLE_USER");
                    assertThat(authority.getCategory()).isEqualTo(AuthorityCategory.COMMON_USER);
                })
                .verifyComplete();
    }

    @DisplayName("Given AuthorityCategory ADMINISTRATOR load only one Authority")
    @Test
    void loadOneAdministratorAuthority() {
        Flux<Authority> flux = new AuthorityServiceStaticImpl().getAuthoritiesByCategory(AuthorityCategory.ADMINISTRATOR);

        StepVerifier.create(flux)
                .assertNext(authority -> {
                    assertThat(authority.getId()).isNull();
                    assertThat(authority.getName()).isEqualTo("ROLE_ADMIN");
                    assertThat(authority.getCategory()).isEqualTo(AuthorityCategory.ADMINISTRATOR);
                })
                .verifyComplete();
    }
}