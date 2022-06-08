package com.trading.journal.authentication.authority.impl;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.verification.service.impl.service.impl.AuthorityServiceStaticImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorityServiceStaticImplTest {

    @DisplayName("Given AuthorityCategory COMMON_USER load only one Authority")
    @Test
    void loadOneCommonUserAuthority() {
        List<Authority> authorities = new AuthorityServiceStaticImpl().getAuthoritiesByCategory(AuthorityCategory.COMMON_USER);

        assertThat(authorities).hasSize(1);
        Authority authority = authorities.get(0);
        assertThat(authority.getId()).isNull();
        assertThat(authority.getName()).isEqualTo("ROLE_USER");
        assertThat(authority.getCategory()).isEqualTo(AuthorityCategory.COMMON_USER);
    }

    @DisplayName("Given AuthorityCategory ADMINISTRATOR load only one Authority")
    @Test
    void loadOneAdministratorAuthority() {
        List<Authority> authorities = new AuthorityServiceStaticImpl().getAuthoritiesByCategory(AuthorityCategory.ADMINISTRATOR);

        assertThat(authorities).hasSize(1);
        Authority authority = authorities.get(0);
        assertThat(authority.getId()).isNull();
        assertThat(authority.getName()).isEqualTo("ROLE_ADMIN");
        assertThat(authority.getCategory()).isEqualTo(AuthorityCategory.ADMINISTRATOR);
    }
}