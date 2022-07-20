package com.trading.journal.authentication.userauthority.service.impl;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.service.AuthorityService;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UserAuthorityServiceImplTest {

    @Mock
    UserAuthorityRepository userAuthorityRepository;

    @Mock
    AuthorityService authorityService;

    @InjectMocks
    UserAuthorityServiceImpl userAuthorityService;

    @DisplayName("Given application user for ONE authority when saving common authorities, save user authorities")
    @Test
    void saveOneCommonAuthority() {
        ApplicationUser applicationUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        Authority authority = Authority.builder().id(1L).category(AuthorityCategory.COMMON_USER).name("USER").build();
        when(authorityService.getAuthoritiesByCategory(AuthorityCategory.COMMON_USER)).thenReturn(singletonList(authority));

        UserAuthority userAuthority = new UserAuthority(applicationUser, authority);
        when(userAuthorityRepository.save(any())).thenReturn(userAuthority);

        List<UserAuthority> userAuthorities = userAuthorityService.saveCommonUserAuthorities(applicationUser);
        assertThat(userAuthorities).hasSize(1);
        assertThat(userAuthorities.get(0)).isEqualTo(userAuthority);
    }

    @DisplayName("Given application user for TWO authority when saving common authorities, save user authorities")
    @Test
    void saveTwoCommonAuthority() {
        ApplicationUser applicationUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        Authority authority1 = Authority.builder().id(1L).category(AuthorityCategory.COMMON_USER).name("USER").build();
        Authority authority2 = Authority.builder().id(2L).category(AuthorityCategory.COMMON_USER).name("ADMIN").build();
        when(authorityService.getAuthoritiesByCategory(AuthorityCategory.COMMON_USER)).thenReturn(Arrays.asList(authority1, authority2));

        UserAuthority userAuthority1 = new UserAuthority(applicationUser, authority1);
        UserAuthority userAuthority2 = new UserAuthority(applicationUser, authority2);
        when(userAuthorityRepository.save(userAuthority1)).thenReturn(userAuthority1);
        when(userAuthorityRepository.save(userAuthority2)).thenReturn(userAuthority2);

        List<UserAuthority> userAuthorities = userAuthorityService.saveCommonUserAuthorities(applicationUser);
        assertThat(userAuthorities).hasSize(2);

        verify(userAuthorityRepository, times(2)).save(any());
    }

    @DisplayName("Given application user for admin authority when saving admin authorities, save user authorities")
    @Test
    void saveOneAdminAuthority() {
        ApplicationUser applicationUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        Authority authorityAdmin = Authority.builder().id(1L).category(AuthorityCategory.ADMINISTRATOR).name("ADMIN").build();
        Authority authorityUser = Authority.builder().id(1L).category(AuthorityCategory.COMMON_USER).name("USER").build();
        when(authorityService.getAll()).thenReturn(Arrays.asList(authorityAdmin, authorityUser));

        UserAuthority userAuthorityUser = new UserAuthority(applicationUser, authorityAdmin);
        UserAuthority userAuthorityAdmin = new UserAuthority(applicationUser, authorityUser);
        when(userAuthorityRepository.save(userAuthorityUser)).thenReturn(userAuthorityUser);
        when(userAuthorityRepository.save(userAuthorityAdmin)).thenReturn(userAuthorityAdmin);

        userAuthorityService.saveAdminUserAuthorities(applicationUser);

        verify(userAuthorityRepository, times(2)).save(any());
    }

    @DisplayName("Add new authority to the user")
    @Test
    void addAuthorities() {
        AuthoritiesChange authoritiesChange = new AuthoritiesChange(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));

        when(authorityService.getByName("ROLE_USER")).thenReturn(Optional.of(new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")));
        when(authorityService.getByName("ROLE_ADMIN")).thenReturn(Optional.of(new Authority(2L, AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN")));

        ApplicationUser applicationUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))))
                .build();

        userAuthorityService.addAuthorities(applicationUser, authoritiesChange);

        verify(userAuthorityRepository).save(any());
    }

    @DisplayName("Add new authority to the user but one of the authority requested to delete is not in user collection")
    @Test
    void addAuthorities2() {
        AuthoritiesChange authoritiesChange = new AuthoritiesChange(Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ANOTHER_ROLE"));

        when(authorityService.getByName("ROLE_USER")).thenReturn(Optional.of(new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")));
        when(authorityService.getByName("ROLE_ADMIN")).thenReturn(Optional.of(new Authority(2L, AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN")));
        ApplicationUser applicationUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(List.of(
                        new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))
                ))
                .build();

        userAuthorityService.addAuthorities(applicationUser, authoritiesChange);

        verify(userAuthorityRepository).save(any());
    }

    @DisplayName("Add two new authorities to the user")
    @Test
    void addTwoAuthorities() {
        AuthoritiesChange authoritiesChange = new AuthoritiesChange(Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ANOTHER_ROLE"));

        when(authorityService.getByName("ROLE_USER")).thenReturn(Optional.of(new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")));
        when(authorityService.getByName("ROLE_ADMIN")).thenReturn(Optional.of(new Authority(2L, AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN")));
        when(authorityService.getByName("ANOTHER_ROLE")).thenReturn(Optional.of(new Authority(3L, AuthorityCategory.ADMINISTRATOR, "ANOTHER_ROLE")));

        ApplicationUser applicationUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))))
                .build();

        userAuthorityService.addAuthorities(applicationUser, authoritiesChange);

        verify(userAuthorityRepository, times(2)).save(any());
    }

    @DisplayName("No new authority to the user")
    @Test
    void doNotAddAuthorities() {
        AuthoritiesChange authoritiesChange = new AuthoritiesChange(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));

        when(authorityService.getByName("ROLE_USER")).thenReturn(Optional.of(new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")));
        when(authorityService.getByName("ROLE_ADMIN")).thenReturn(Optional.of(new Authority(2L, AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN")));

        ApplicationUser applicationUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(List.of(
                        new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")),
                        new UserAuthority(null, new Authority(2L, AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN"))))
                .build();

        userAuthorityService.addAuthorities(applicationUser, authoritiesChange);

        verify(userAuthorityRepository, never()).save(any());
    }

    @DisplayName("Delete authority from the user")
    @Test
    void deleteAuthorities() {
        AuthoritiesChange authoritiesChange = new AuthoritiesChange(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));

        when(authorityService.getByName("ROLE_USER")).thenReturn(Optional.of(new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")));
        when(authorityService.getByName("ROLE_ADMIN")).thenReturn(Optional.of(new Authority(2L, AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN")));

        ApplicationUser applicationUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))))
                .build();

        userAuthorityService.deleteAuthorities(applicationUser, authoritiesChange);

        verify(userAuthorityRepository).delete(any());
    }

    @DisplayName("Delete authority from the user but one of the authority requested to delete is not in user collection")
    @Test
    void deleteAuthorities2() {
        AuthoritiesChange authoritiesChange = new AuthoritiesChange(Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ANOTHER_ROLE"));

        when(authorityService.getByName("ROLE_USER")).thenReturn(Optional.of(new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")));
        when(authorityService.getByName("ROLE_ADMIN")).thenReturn(Optional.of(new Authority(2L, AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN")));
        when(authorityService.getByName("ANOTHER_ROLE")).thenReturn(Optional.of(new Authority(5L, AuthorityCategory.ADMINISTRATOR, "ANOTHER_ROLE")));

        ApplicationUser applicationUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(List.of(
                        new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")),
                        new UserAuthority(null, new Authority(3L, AuthorityCategory.COMMON_USER, "ANOTHER_ROLE"))
                ))
                .build();

        userAuthorityService.deleteAuthorities(applicationUser, authoritiesChange);

        verify(userAuthorityRepository).delete(any());
    }

    @DisplayName("Delete two new authorities from the user")
    @Test
    void deleteTwoAuthorities() {
        AuthoritiesChange authoritiesChange = new AuthoritiesChange(Arrays.asList("ROLE_USER", "ANOTHER_ROLE"));

        when(authorityService.getByName("ROLE_USER")).thenReturn(Optional.of(new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")));
        when(authorityService.getByName("ANOTHER_ROLE")).thenReturn(Optional.of(new Authority(3L, AuthorityCategory.ADMINISTRATOR, "ANOTHER_ROLE")));

        ApplicationUser applicationUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))
                        , new UserAuthority(null, new Authority(2L, AuthorityCategory.COMMON_USER, "ROLE_ADMIN"))
                        , new UserAuthority(null, new Authority(3L, AuthorityCategory.ADMINISTRATOR, "ANOTHER_ROLE"))))
                .build();

        userAuthorityService.deleteAuthorities(applicationUser, authoritiesChange);

        verify(userAuthorityRepository, times(2)).delete(any());
    }

    @DisplayName("No deleted authorities from the user")
    @Test
    void doNotDeleteAuthorities() {
        AuthoritiesChange authoritiesChange = new AuthoritiesChange(Arrays.asList("ANOTHER_ROLE_USER", "ANOTHER_ROLE_ADMIN"));

        when(authorityService.getByName("ANOTHER_ROLE_USER")).thenReturn(Optional.empty());
        when(authorityService.getByName("ANOTHER_ROLE_ADMIN")).thenReturn(Optional.empty());

        ApplicationUser applicationUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(List.of(
                        new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))
                        , new UserAuthority(null, new Authority(2L, AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN"))
                ))
                .build();

        userAuthorityService.deleteAuthorities(applicationUser, authoritiesChange);

        verify(userAuthorityRepository, never()).delete(any());
    }
}