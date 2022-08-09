package com.trading.journal.authentication.authority.service.impl;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.AuthorityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class OrganisationAuthorityServiceImplTest {

    @Mock
    AuthorityRepository authorityRepository;

    @InjectMocks
    OrganisationAuthorityServiceImpl organisationAuthorityService;

    @DisplayName("Return all authorities that are not ADMINISTRATOR category")
    @Test
    void getAllNonAdmin() {
        when(authorityRepository.getByCategoryNot(AuthorityCategory.ADMINISTRATOR)).thenReturn(
                singletonList(Authority.builder().name("USER").category(AuthorityCategory.COMMON_USER).id(1L).build())
        );

        List<Authority> authorities = organisationAuthorityService.getAllNonAdmin();
        assertThat(authorities).hasSize(1);
    }
}