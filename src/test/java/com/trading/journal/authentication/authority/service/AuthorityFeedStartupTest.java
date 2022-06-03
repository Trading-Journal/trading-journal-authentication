package com.trading.journal.authentication.authority.service;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AuthorityFeedStartupTest {

    @Mock
    AuthorityRepository authorityRepository;

    @InjectMocks
    AuthorityFeedStartup authorityFeedStartup;

    @DisplayName("When none authorities are in database save them all")
    @Test
    void saveAll() {
        Authority user = new Authority(AuthorityCategory.COMMON_USER, "ROLE_USER");
        Authority admin = new Authority(AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN");

        when(authorityRepository.getByName("ROLE_USER")).thenReturn(Mono.empty());
        when(authorityRepository.getByName("ROLE_ADMIN")).thenReturn(Mono.empty());

        when(authorityRepository.save(user)).thenReturn(Mono.just(user));
        when(authorityRepository.save(admin)).thenReturn(Mono.just(admin));

        authorityFeedStartup.onApplicationEvent(null);

        verify(authorityRepository, times(2)).getByName(anyString());
        verify(authorityRepository, times(2)).save(any());
    }

    @DisplayName("When all authorities are in database do not save them")
    @Test
    void saveNone() {
        Authority user = new Authority(AuthorityCategory.COMMON_USER, "ROLE_USER");
        Authority admin = new Authority(AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN");

        when(authorityRepository.getByName("ROLE_USER")).thenReturn(Mono.just(user));
        when(authorityRepository.getByName("ROLE_ADMIN")).thenReturn(Mono.just(admin));

        authorityFeedStartup.onApplicationEvent(null);

        verify(authorityRepository, times(2)).getByName(anyString());
        verify(authorityRepository, never()).save(any());
    }

    @DisplayName("When ROLE_USER authority is in database do not save this but save ROLE_ADMIN which is not in database")
    @Test
    void saveROLE_USER() {
        Authority user = new Authority(AuthorityCategory.COMMON_USER, "ROLE_USER");
        Authority admin = new Authority(AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN");

        when(authorityRepository.getByName("ROLE_USER")).thenReturn(Mono.just(user));
        when(authorityRepository.getByName("ROLE_ADMIN")).thenReturn(Mono.empty());

        when(authorityRepository.save(admin)).thenReturn(Mono.just(admin));

        authorityFeedStartup.onApplicationEvent(null);

        verify(authorityRepository, times(2)).getByName(anyString());
        verify(authorityRepository, never()).save(user);
        verify(authorityRepository).save(admin);
    }

    @DisplayName("When ROLE_ADMIN authority is in database do not save this but save ROLE_USER which is not in database")
    @Test
    void saveROLE_ADMIN() {
        Authority user = new Authority(AuthorityCategory.COMMON_USER, "ROLE_USER");
        Authority admin = new Authority(AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN");

        when(authorityRepository.getByName("ROLE_USER")).thenReturn(Mono.empty());
        when(authorityRepository.getByName("ROLE_ADMIN")).thenReturn(Mono.just(admin));

        when(authorityRepository.save(user)).thenReturn(Mono.just(user));

        authorityFeedStartup.onApplicationEvent(null);

        verify(authorityRepository, times(2)).getByName(anyString());
        verify(authorityRepository, never()).save(admin);
        verify(authorityRepository).save(user);
    }
}