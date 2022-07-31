package com.trading.journal.authentication.authority;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthoritiesHelperTest {

    @DisplayName("Check name and values for each static authority")
    @Test
    void nameAndValues() {
        assertThat(AuthoritiesHelper.ROLE_USER.getLabel()).isEqualTo("ROLE_USER");
        assertThat(AuthoritiesHelper.ROLE_USER.getCategory()).isEqualTo(AuthorityCategory.COMMON_USER);

        assertThat(AuthoritiesHelper.ROLE_ADMIN.getLabel()).isEqualTo("ROLE_ADMIN");
        assertThat(AuthoritiesHelper.ROLE_ADMIN.getCategory()).isEqualTo(AuthorityCategory.ADMINISTRATOR);

        assertThat(AuthoritiesHelper.TENANCY_ADMIN.getLabel()).isEqualTo("TENANCY_ADMIN");
        assertThat(AuthoritiesHelper.TENANCY_ADMIN.getCategory()).isEqualTo(AuthorityCategory.ORGANISATION);
    }

    @DisplayName("Get authorities by COMMON_USER category")
    @Test
    void getByCategoryCOMMON_USER() {
        List<AuthoritiesHelper> list = AuthoritiesHelper.getByCategory(AuthorityCategory.COMMON_USER);
        assertThat(list).hasSize(1);
        assertThat(list).extracting(AuthoritiesHelper::getLabel).containsExactly("ROLE_USER");
    }

    @DisplayName("Get authorities by ADMINISTRATOR category")
    @Test
    void getByCategoryADMINISTRATOR() {
        List<AuthoritiesHelper> list = AuthoritiesHelper.getByCategory(AuthorityCategory.ADMINISTRATOR);
        assertThat(list).hasSize(1);
        assertThat(list).extracting(AuthoritiesHelper::getLabel).containsExactly("ROLE_ADMIN");
    }

    @DisplayName("Get authorities by ORGANISATION_ADMINISTRATOR category")
    @Test
    void getByCategoryORGANISATION_ADMINISTRATOR() {
        List<AuthoritiesHelper> list = AuthoritiesHelper.getByCategory(AuthorityCategory.ORGANISATION);
        assertThat(list).hasSize(1);
        assertThat(list).extracting(AuthoritiesHelper::getLabel).containsExactly("TENANCY_ADMIN");
    }
}