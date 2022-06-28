package com.trading.journal.authentication.authority.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.AuthorityRepository;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AuthorityServiceImplTest {
    @Mock
    AuthorityRepository authorityRepository;

    @Mock
    UserAuthorityRepository userAuthorityRepository;

    @InjectMocks
    AuthorityServiceImpl authorityService;

    @DisplayName("Return all authorities by category")
    @Test
    void getAuthoritiesByCategory() {
        AuthorityCategory category = AuthorityCategory.ADMINISTRATOR;
        when(authorityRepository.getByCategory(category)).thenReturn(
                singletonList(Authority.builder().name("ADMIN").category(category).id(1L).build())
        );

        List<Authority> authorities = authorityService.getAuthoritiesByCategory(category);
        assertThat(authorities).hasSize(1);
    }

    @DisplayName("Return all authorities")
    @Test
    void getAll() {
        when(authorityRepository.findAll()).thenReturn(
                singletonList(Authority.builder().name("ADMIN").category(AuthorityCategory.ADMINISTRATOR).id(1L).build())
        );

        List<Authority> authorities = authorityService.getAll();
        assertThat(authorities).hasSize(1);
    }

    @DisplayName("Return Authority by name")
    @Test
    void getByName() {
        String name = "ROLE_ADMIN";
        when(authorityRepository.getByName(name)).thenReturn(
                of(Authority.builder().name("ROLE_ADMIN").category(AuthorityCategory.ADMINISTRATOR).id(1L).build())
        );

        Optional<Authority> authority = authorityService.getByName(name);
        assertThat(authority).isNotEmpty();
    }

    @DisplayName("Return all authorities categories")
    @Test
    void getAllCategories() {
        List<AuthorityCategory> categories = authorityService.getAllCategories();

        assertThat(categories).hasSize(2);

        assertThat(categories).containsExactlyInAnyOrder(AuthorityCategory.ADMINISTRATOR, AuthorityCategory.COMMON_USER);
    }

    @DisplayName("Return Authority by id")
    @Test
    void getById() {
        Long id = 1L;
        when(authorityRepository.findById(id)).thenReturn(
                of(Authority.builder().name("ROLE_ADMIN").category(AuthorityCategory.ADMINISTRATOR).id(id).build())
        );

        Authority authority = authorityService.getById(id);
        assertThat(authority).isNotNull();
    }

    @DisplayName("Getting Authority by id when it is not found return an exception")
    @Test
    void getByIdNotFound() {
        Long id = 1L;
        when(authorityRepository.findById(id)).thenReturn(empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authorityService.getById(id));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Authority id not found");
    }

    @DisplayName("Add a new Authority return added authority")
    @Test
    void add() {
        Authority authority = new Authority(AuthorityCategory.COMMON_USER, "USER");
        Authority authoritySaved = new Authority(1L, AuthorityCategory.COMMON_USER, "USER");

        when(authorityRepository.getByName(authority.getName())).thenReturn(empty());
        when(authorityRepository.save(authority)).thenReturn(authoritySaved);

        Authority authorityReturned = authorityService.add(authority);
        assertThat(authorityReturned).isSameAs(authoritySaved);
    }

    @DisplayName("Add a new Authority with a existing name return an exception")
    @Test
    void addException() {
        Authority authority = new Authority(AuthorityCategory.COMMON_USER, "USER");
        Authority authoritySaved = new Authority(1L, AuthorityCategory.COMMON_USER, "USER");

        when(authorityRepository.getByName(authority.getName())).thenReturn(of(authoritySaved));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authorityService.add(authority));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getStatusText()).isEqualTo("Authority name already exists");

        verify(authorityRepository, never()).save(any());
    }


    @DisplayName("Update a Authority return updated authority")
    @Test
    void update() {
        Long id = 1L;
        Authority authority = new Authority(AuthorityCategory.COMMON_USER, "USER_AFTER");
        Authority authorityStored = new Authority(1L, AuthorityCategory.COMMON_USER, "USER_BEFORE");
        Authority authorityUpdated = new Authority(1L, AuthorityCategory.COMMON_USER, "USER_AFTER");

        when(authorityRepository.findById(id)).thenReturn(of(authorityStored));
        when(authorityRepository.getByNameAndIdNot(authority.getName(), authorityStored.getId())).thenReturn(empty());
        when(authorityRepository.save(any())).thenReturn(authorityUpdated);

        Authority authorityReturned = authorityService.update(id, authority);
        assertThat(authorityReturned).isSameAs(authorityUpdated);
    }

    @DisplayName("Update a Authority but the authority id is not found return exception")
    @Test
    void updateNotFound() {
        Long id = 1L;
        Authority authority = new Authority(AuthorityCategory.COMMON_USER, "USER_AFTER");

        when(authorityRepository.findById(id)).thenReturn(empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authorityService.update(id, authority));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Authority id not found");

        verify(authorityRepository, never()).getByNameAndIdNot(anyString(), anyLong());
        verify(authorityRepository, never()).save(any());
    }

    @DisplayName("Update a Authority when there is another authority with same name and different id return exception")
    @Test
    void updateConflict() {
        Long id = 1L;
        Authority authority = new Authority(AuthorityCategory.COMMON_USER, "USER");
        Authority authorityStored = new Authority(1L, AuthorityCategory.COMMON_USER, "USER_BEFORE");
        Authority authoritySameName = new Authority(1L, AuthorityCategory.COMMON_USER, "USER_BEFORE");

        when(authorityRepository.findById(id)).thenReturn(of(authorityStored));
        when(authorityRepository.getByNameAndIdNot(authority.getName(), authorityStored.getId())).thenReturn(of(authoritySameName));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authorityService.update(id, authority));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getStatusText()).isEqualTo("Authority name already exists");

        verify(authorityRepository, never()).save(any());
    }

    @DisplayName("Delete a Authority")
    @Test
    void delete() {
        Long id = 1L;
        Authority authorityStored = new Authority(1L, AuthorityCategory.COMMON_USER, "USER_BEFORE");
        when(authorityRepository.findById(id)).thenReturn(of(authorityStored));
        when(userAuthorityRepository.existsByAuthorityId(id)).thenReturn(false);

        authorityService.delete(id);

        verify(authorityRepository).deleteById(id);
    }

    @DisplayName("Delete a Authority but the authority id is not found return exception")
    @Test
    void deleteNotFound() {
        Long id = 1L;
        when(authorityRepository.findById(id)).thenReturn(empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authorityService.delete(id));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Authority id not found");

        verify(authorityRepository, never()).deleteById(anyLong());
        verify(userAuthorityRepository, never()).existsByAuthorityId(anyLong());
    }

    @DisplayName("Delete a Authority but this authority is used for some user return exception")
    @Test
    void deleteUsedException() {
        Long id = 1L;
        Authority authorityStored = new Authority(1L, AuthorityCategory.COMMON_USER, "USER_BEFORE");
        when(authorityRepository.findById(id)).thenReturn(of(authorityStored));
        when(userAuthorityRepository.existsByAuthorityId(id)).thenReturn(true);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authorityService.delete(id));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Authority is used by one or more user");

        verify(authorityRepository, never()).deleteById(anyLong());
    }
}