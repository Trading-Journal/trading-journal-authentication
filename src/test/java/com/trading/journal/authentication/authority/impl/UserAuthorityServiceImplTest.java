package com.trading.journal.authentication.authority.impl;

import com.trading.journal.authentication.authority.*;
import com.trading.journal.authentication.user.ApplicationUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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
        when(authorityService.getAuthoritiesByCategory(AuthorityCategory.COMMON_USER)).thenReturn(Flux.just(authority));

        UserAuthority userAuthority = new UserAuthority(applicationUser.getId(), authority.getName(), authority.getId());
        when(userAuthorityRepository.save(userAuthority)).thenReturn(Mono.just(userAuthority));

        Mono<UserAuthority> mono = userAuthorityService.saveCommonUserAuthorities(applicationUser);
        StepVerifier.create(mono)
                .expectNextCount(1L)
                .verifyComplete();

        verify(userAuthorityRepository).save(any());
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
        Authority authority2 = Authority.builder().id(2L).category(AuthorityCategory.ADMINISTRATOR).name("ADMIN").build();
        when(authorityService.getAuthoritiesByCategory(AuthorityCategory.COMMON_USER)).thenReturn(Flux.just(authority1, authority2));

        UserAuthority userAuthority1 = new UserAuthority(applicationUser.getId(), authority1.getName(), authority1.getId());
        UserAuthority userAuthority2 = new UserAuthority(applicationUser.getId(), authority2.getName(), authority2.getId());
        when(userAuthorityRepository.save(userAuthority1)).thenReturn(Mono.just(userAuthority1));
        when(userAuthorityRepository.save(userAuthority2)).thenReturn(Mono.just(userAuthority2));

        Mono<UserAuthority> mono = userAuthorityService.saveCommonUserAuthorities(applicationUser);
        StepVerifier.create(mono)
                .expectNextCount(1L)
                .verifyComplete();

        verify(userAuthorityRepository, times(2)).save(any());
    }

    @DisplayName("Given application user to load one authority, return a list of authorities with one item")
    @Test
    void loadOneAuthorityFromApplicationUser() {
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
        when(userAuthorityRepository.findByUserId(applicationUser.getId())).thenReturn(Flux.just(userAuthority));

        Mono<List<UserAuthority>> mono = userAuthorityService.loadList(applicationUser);
        StepVerifier.create(mono)
                .assertNext(list -> assertThat(list).hasSize(1))
                .verifyComplete();
    }

    @DisplayName("Given application user to load three authority, return a list of authorities with one item")
    @Test
    void loadThreeAuthorityFromApplicationUser() {
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
        when(userAuthorityRepository.findByUserId(applicationUser.getId())).thenReturn(Flux.just(userAuthority1, userAuthority2, userAuthority3));

        Mono<List<UserAuthority>> mono = userAuthorityService.loadList(applicationUser);
        StepVerifier.create(mono)
                .assertNext(list -> assertThat(list).hasSize(3))
                .verifyComplete();
    }

    @DisplayName("Given user id to load one authority, return a list of authorities with one item")
    @Test
    void loadOneAuthorityFromUserId() {
        UserAuthority userAuthority = new UserAuthority(1L, "User", 1L);
        when(userAuthorityRepository.findByUserId(1L)).thenReturn(Flux.just(userAuthority));

        Mono<List<UserAuthority>> mono = userAuthorityService.loadList(1L);
        StepVerifier.create(mono)
                .assertNext(list -> assertThat(list).hasSize(1))
                .verifyComplete();
    }

    @DisplayName("Given user id to load three authority, return a list of authorities with one item")
    @Test
    void loadThreeAuthorityFromUserId() {
        UserAuthority userAuthority1 = new UserAuthority(1L, "User", 1L);
        UserAuthority userAuthority2 = new UserAuthority(2L, "Admin", 1L);
        UserAuthority userAuthority3 = new UserAuthority(3L, "Other", 1L);
        when(userAuthorityRepository.findByUserId(1L)).thenReturn(Flux.just(userAuthority1, userAuthority2, userAuthority3));

        Mono<List<UserAuthority>> mono = userAuthorityService.loadList(1L);
        StepVerifier.create(mono)
                .assertNext(list -> assertThat(list).hasSize(3))
                .verifyComplete();
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
        when(userAuthorityRepository.findByUserId(applicationUser.getId())).thenReturn(Flux.just(userAuthority));

        Mono<List<SimpleGrantedAuthority>> mono = userAuthorityService.loadListAsSimpleGrantedAuthority(applicationUser);
        StepVerifier.create(mono)
                .assertNext(list -> assertThat(list).hasSize(1))
                .verifyComplete();
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
        when(userAuthorityRepository.findByUserId(applicationUser.getId())).thenReturn(Flux.just(userAuthority1, userAuthority2, userAuthority3));

        Mono<List<SimpleGrantedAuthority>> mono = userAuthorityService.loadListAsSimpleGrantedAuthority(applicationUser);
        StepVerifier.create(mono)
                .assertNext(list -> assertThat(list).hasSize(3))
                .verifyComplete();
    }
}