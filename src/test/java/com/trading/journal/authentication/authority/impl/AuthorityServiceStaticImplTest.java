package com.trading.journal.authentication.authority.impl;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.verification.service.impl.service.impl.AuthorityServiceStaticImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    @DisplayName("Getting Authority by name return it successfully")
    @Test
    void getByName() {
        Optional<Authority> role_user = new AuthorityServiceStaticImpl().getByName("ROLE_USER");
        assertThat(role_user).isNotEmpty();
        assertThat(role_user.get().getCategory()).isEqualTo( AuthorityCategory.COMMON_USER);

        Optional<Authority> role_admin = new AuthorityServiceStaticImpl().getByName("ROLE_ADMIN");
        assertThat(role_admin).isNotEmpty();
        assertThat(role_admin.get().getCategory()).isEqualTo( AuthorityCategory.ADMINISTRATOR);
    }

    @DisplayName("Getting Authority by name return empty because it does not exist")
    @ParameterizedTest
    @MethodSource("invalidNames")
    void getByNameEmpty(String name) {
        Optional<Authority> authority = new AuthorityServiceStaticImpl().getByName(name);
        assertThat(authority).isEmpty();
    }

    public static Stream<String> invalidNames(){
        return Stream.of(null, "", "ANY", "USER_ROLE", "ROLE_USERS");
    }
}