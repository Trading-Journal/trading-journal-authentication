package com.trading.journal.authentication.configuration;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.service.AuthorityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class LoadAuthoritiesTest {

    @Mock
    AuthorityService authorityService;

    @InjectMocks
    LoadAuthorities loadAuthorities;

    @DisplayName("Load all authorities in the map")
    @Test
    void load() {
        when(authorityService.getAuthoritiesByCategory(AuthorityCategory.COMMON_USER)).thenReturn(
                singletonList(new Authority(1L, AuthorityCategory.COMMON_USER, "USER"))
        );

        when(authorityService.getAuthoritiesByCategory(AuthorityCategory.ADMINISTRATOR)).thenReturn(
                singletonList(new Authority(2L, AuthorityCategory.ADMINISTRATOR, "ADMIN"))
        );

        Map<AuthorityCategory, String[]> authoritiesMap = loadAuthorities.getAuthorityCategoryMap();

        assertThat(authoritiesMap.keySet()).containsExactlyInAnyOrder(AuthorityCategory.COMMON_USER, AuthorityCategory.ADMINISTRATOR);

        assertThat(authoritiesMap.values()).containsExactlyInAnyOrder(new String[]{"USER"},new String[]{"ADMIN"});
    }

    @DisplayName("Load authorities is empty return exception")
    @Test
    void loadEmpty() {
        when(authorityService.getAuthoritiesByCategory(AuthorityCategory.ADMINISTRATOR)).thenReturn(emptyList());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> loadAuthorities.getAuthorityCategoryMap());
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getStatusText()).isEqualTo("No authorities found in the database, please load it");
    }

    @DisplayName("Load authorities is null return exception")
    @Test
    void loadNull() {
        when(authorityService.getAuthoritiesByCategory(AuthorityCategory.ADMINISTRATOR)).thenReturn(null);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> loadAuthorities.getAuthorityCategoryMap());
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getStatusText()).isEqualTo("No authorities found in the database, please load it");
    }
}