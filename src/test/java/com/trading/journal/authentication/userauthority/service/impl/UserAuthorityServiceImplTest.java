package com.trading.journal.authentication.userauthority.service.impl;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.verification.service.impl.service.AuthorityService;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;
import com.trading.journal.authentication.user.ApplicationUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "12345679",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.emptyList(),
                LocalDateTime.now());

        Authority authority = Authority.builder().id(1L).category(AuthorityCategory.COMMON_USER).name("USER").build();
        when(authorityService.getAuthoritiesByCategory(AuthorityCategory.COMMON_USER)).thenReturn(singletonList(authority));

        UserAuthority userAuthority = new UserAuthority(applicationUser.getId(), authority.getName(), authority.getId());
        when(userAuthorityRepository.save(userAuthority)).thenReturn(userAuthority);

        List<UserAuthority> userAuthorities = userAuthorityService.saveCommonUserAuthorities(applicationUser);
        assertThat(userAuthorities).hasSize(1);
        assertThat(userAuthorities.get(0)).isEqualTo(userAuthority);
    }

    @DisplayName("Given application user for TWO authority when saving common authorities, save user authorities")
    @Test
    void saveTwoCommonAuthority() {
        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "12345679",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.emptyList(),
                LocalDateTime.now());

        Authority authority1 = Authority.builder().id(1L).category(AuthorityCategory.COMMON_USER).name("USER").build();
        Authority authority2 = Authority.builder().id(2L).category(AuthorityCategory.COMMON_USER).name("ADMIN").build();
        when(authorityService.getAuthoritiesByCategory(AuthorityCategory.COMMON_USER)).thenReturn(Arrays.asList(authority1, authority2));

        UserAuthority userAuthority1 = new UserAuthority(applicationUser.getId(), authority1.getName(), authority1.getId());
        UserAuthority userAuthority2 = new UserAuthority(applicationUser.getId(), authority2.getName(), authority2.getId());
        when(userAuthorityRepository.save(userAuthority1)).thenReturn(userAuthority1);
        when(userAuthorityRepository.save(userAuthority2)).thenReturn(userAuthority2);

        List<UserAuthority> userAuthorities = userAuthorityService.saveCommonUserAuthorities(applicationUser);
        assertThat(userAuthorities).hasSize(2);

        verify(userAuthorityRepository, times(2)).save(any());
    }

    @DisplayName("Given application user for admin authority when saving admin authorities, save user authorities")
    @Test
    void saveOneAdminAuthority() {
        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "12345679",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.emptyList(),
                LocalDateTime.now());

        Authority authorityAdmin = Authority.builder().id(1L).category(AuthorityCategory.ADMINISTRATOR).name("ADMIN").build();
        Authority authorityUser = Authority.builder().id(1L).category(AuthorityCategory.COMMON_USER).name("USER").build();
        when(authorityService.getAll()).thenReturn(Arrays.asList(authorityAdmin, authorityUser));

        UserAuthority userAuthorityUser = new UserAuthority(applicationUser.getId(), authorityAdmin.getName(), authorityAdmin.getId());
        UserAuthority userAuthorityAdmin = new UserAuthority(applicationUser.getId(), authorityUser.getName(), authorityUser.getId());
        when(userAuthorityRepository.save(userAuthorityUser)).thenReturn(userAuthorityUser);
        when(userAuthorityRepository.save(userAuthorityAdmin)).thenReturn(userAuthorityAdmin);

        userAuthorityService.saveAdminUserAuthorities(applicationUser);

        verify(userAuthorityRepository, times(2)).save(any());
    }

    @DisplayName("Given user id to load one authority, return a list of authorities with one item")
    @Test
    void loadOneAuthorityFromUserId() {
        UserAuthority userAuthority = new UserAuthority(1L, "User", 1L);
        when(userAuthorityRepository.findByUserId(1L)).thenReturn(singletonList(userAuthority));

        List<UserAuthority> userAuthorities = userAuthorityService.getByUserId(1L);
        assertThat(userAuthorities).hasSize(1);
    }

    @DisplayName("Given user id to load three authority, return a list of authorities with one item")
    @Test
    void loadThreeAuthorityFromUserId() {
        UserAuthority userAuthority1 = new UserAuthority(1L, "User", 1L);
        UserAuthority userAuthority2 = new UserAuthority(2L, "Admin", 1L);
        UserAuthority userAuthority3 = new UserAuthority(3L, "Other", 1L);
        when(userAuthorityRepository.findByUserId(1L)).thenReturn(Arrays.asList(userAuthority1, userAuthority2, userAuthority3));

        List<UserAuthority> userAuthorities = userAuthorityService.getByUserId(1L);
        assertThat(userAuthorities).hasSize(3);
    }

    @DisplayName("Given application user to load one SimpleGrantedAuthority, return a list of SimpleGrantedAuthority with one item")
    @Test
    void loadOneSimpleGrantedAuthority() {
        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "12345679",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.emptyList(),
                LocalDateTime.now());

        UserAuthority userAuthority = new UserAuthority(1L, "User", applicationUser.getId());
        when(userAuthorityRepository.findByUserId(applicationUser.getId())).thenReturn(singletonList(userAuthority));

        List<SimpleGrantedAuthority> simpleGrantedAuthorities = userAuthorityService.loadListAsSimpleGrantedAuthority(applicationUser);
        assertThat(simpleGrantedAuthorities).hasSize(1);
    }

    @DisplayName("Given application user to load three SimpleGrantedAuthority, return a list of SimpleGrantedAuthority with one item")
    @Test
    void loadThreeSimpleGrantedAuthority() {
        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "12345679",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.emptyList(),
                LocalDateTime.now());

        UserAuthority userAuthority1 = new UserAuthority(1L, "User", applicationUser.getId());
        UserAuthority userAuthority2 = new UserAuthority(2L, "Admin", applicationUser.getId());
        UserAuthority userAuthority3 = new UserAuthority(3L, "Other", applicationUser.getId());
        when(userAuthorityRepository.findByUserId(applicationUser.getId())).thenReturn(Arrays.asList(userAuthority1, userAuthority2, userAuthority3));

        List<SimpleGrantedAuthority> simpleGrantedAuthorities = userAuthorityService.loadListAsSimpleGrantedAuthority(applicationUser);
        assertThat(simpleGrantedAuthorities).hasSize(3);
    }
}