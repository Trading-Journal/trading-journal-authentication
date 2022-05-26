package com.trading.journal.authentication.user.impl;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.time.LocalDateTime;
import java.util.Collections;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.user.*;
import com.trading.journal.authentication.registration.UserRegistration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class ApplicationUserServiceImplTest {

    @Mock
    ApplicationUserRepository applicationUserRepository;

    @Mock
    UserAuthorityService userAuthorityService;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    ApplicationUserServiceImpl applicationUserServiceImpl;

    @Test
    @DisplayName("When create user return user response")
    void createUser() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        ApplicationUser appUser = new ApplicationUser(
                1L,
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.singletonList(new UserAuthority(1L, 1L, "ROLE_USER")),
                LocalDateTime.now());

        when(applicationUserRepository.countByEmail(anyString())).thenReturn(Mono.just(0));
        when(applicationUserRepository.countByUserName(anyString())).thenReturn(Mono.just(0));
        when(userAuthorityService.saveBasicUserAuthorities(any())).thenReturn(Mono.just(new UserAuthority(1L, 1L, "USER")));
        when(encoder.encode(anyString())).thenReturn("sdsa54ds56a4ds564d");
        when(applicationUserRepository.save(any())).thenReturn(Mono.just(appUser));
        when(applicationUserRepository.findById(anyLong())).thenReturn(Mono.just(appUser));

        Mono<ApplicationUser> userMono = applicationUserServiceImpl.createNewUser(userRegistration);
        StepVerifier.create(userMono).expectNextCount(1).verifyComplete();
    }

    @Test
    @DisplayName("When create user and user name already exist return exception")
    void userNameAlreadyExist() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        when(applicationUserRepository.countByUserName(anyString())).thenReturn(Mono.just(1));
        when(applicationUserRepository.countByEmail(anyString())).thenReturn(Mono.just(0));

        Mono<ApplicationUser> userMono = applicationUserServiceImpl.createNewUser(userRegistration);
        StepVerifier.create(userMono)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && ((ApplicationException) throwable).getStatusCode().equals(BAD_REQUEST)
                        && ((ApplicationException) throwable)
                                .getStatusText()
                                .equals("User name or email already exist"))
                .verify();

        verify(applicationUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("When create user and email already exist return exception")
    void emailAlreadyExist() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        when(applicationUserRepository.countByUserName(anyString())).thenReturn(Mono.just(0));
        when(applicationUserRepository.countByEmail(anyString())).thenReturn(Mono.just(1));

        Mono<ApplicationUser> userMono = applicationUserServiceImpl.createNewUser(userRegistration);
        StepVerifier.create(userMono)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && ((ApplicationException) throwable).getStatusCode().equals(BAD_REQUEST)
                        && ((ApplicationException) throwable)
                                .getStatusText()
                                .equals("User name or email already exist"))
                .verify();

        verify(applicationUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("When create user and user name and email already exist return exception")
    void userNameAndEmailAlreadyExist() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        when(applicationUserRepository.countByUserName(anyString())).thenReturn(Mono.just(1));
        when(applicationUserRepository.countByEmail(anyString())).thenReturn(Mono.just(1));

        Mono<ApplicationUser> userMono = applicationUserServiceImpl.createNewUser(userRegistration);
        StepVerifier.create(userMono)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && ((ApplicationException) throwable).getStatusCode().equals(BAD_REQUEST)
                        && ((ApplicationException) throwable)
                                .getStatusText()
                                .equals("User name or email already exist"))
                .verify();

        verify(applicationUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("When validate new user with valid data return is valid")
    void isValid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(applicationUserRepository.countByUserName(anyString())).thenReturn(Mono.just(0));
        when(applicationUserRepository.countByEmail(anyString())).thenReturn(Mono.just(0));

        Mono<Boolean> userValid = applicationUserServiceImpl.validateNewUser(userName, email);

        StepVerifier.create(userValid).expectNext(true).verifyComplete();
    }

    @Test
    @DisplayName("When validate new user with invalid data return is invalid")
    void isInvalid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(applicationUserRepository.countByUserName(anyString())).thenReturn(Mono.just(1));
        when(applicationUserRepository.countByEmail(anyString())).thenReturn(Mono.just(0));

        Mono<Boolean> userValid = applicationUserServiceImpl.validateNewUser(userName, email);

        StepVerifier.create(userValid).expectNext(false).verifyComplete();
    }

    @Test
    @DisplayName("When validate new user with valid user name and invalid email return is valid")
    void isEmailInvalid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(applicationUserRepository.countByUserName(anyString())).thenReturn(Mono.just(0));
        when(applicationUserRepository.countByEmail(anyString())).thenReturn(Mono.just(1));

        Mono<Boolean> userValid = applicationUserServiceImpl.validateNewUser(userName, email);

        StepVerifier.create(userValid).expectNext(false).verifyComplete();
    }

    @Test
    @DisplayName("When validate new user with invalid user name and valid email return is valid")
    void isUserNameInvalid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(applicationUserRepository.countByUserName(anyString())).thenReturn(Mono.just(1));
        when(applicationUserRepository.countByEmail(anyString())).thenReturn(Mono.just(1));

        Mono<Boolean> userValid = applicationUserServiceImpl.validateNewUser(userName, email);

        StepVerifier.create(userValid).expectNext(false).verifyComplete();
    }

    @Test
    @DisplayName("When find user by email that exist return user details")
    void userDetails() {
        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.singletonList(new UserAuthority(1L, 1L, "ROLE_USER")),
                LocalDateTime.now());

        when(applicationUserRepository.findByEmail(applicationUser.getEmail())).thenReturn(Mono.just(applicationUser));

        Mono<UserDetails> userDetailsMono = applicationUserServiceImpl.findByUsername(applicationUser.getEmail());
        when(userAuthorityService.loadListOfSimpleGrantedAuthority(applicationUser)).thenReturn(Mono.just(singletonList(new SimpleGrantedAuthority("ROLE_USER"))));

        StepVerifier.create(userDetailsMono)
                .assertNext(details -> {
                    assertThat(details.isAccountNonExpired()).isTrue();
                    assertThat(details.isCredentialsNonExpired()).isTrue();
                    assertThat(details.isEnabled()).isTrue();
                    assertThat(details.isAccountNonLocked()).isTrue();
                    assertThat(details.getAuthorities().toArray()).hasSize(1);
                    assertThat(details.getAuthorities().toArray())
                            .contains(new SimpleGrantedAuthority(AuthoritiesHelper.ROLE_USER));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("When find user by email that does not exist return exception")
    void findUserException() {
        when(applicationUserRepository.findByEmail(anyString())).thenReturn(Mono.empty());

        Mono<UserDetails> responseMono = applicationUserServiceImpl.findByUsername("email@mail.com");
        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable -> throwable instanceof UsernameNotFoundException
                        && throwable.getMessage().equals("User email@mail.com does not exist"))
                .verify();
    }

    @Test
    @DisplayName("When find user by email and the authorities are null return exception")
    void authoritiesNullReturnException() {
        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                null,
                LocalDateTime.now());

        when(applicationUserRepository.findByEmail(applicationUser.getEmail())).thenReturn(Mono.just(applicationUser));
        when(userAuthorityService.loadListOfSimpleGrantedAuthority(applicationUser)).thenReturn(Mono.just(emptyList()));

        Mono<UserDetails> responseMono = applicationUserServiceImpl.findByUsername(applicationUser.getEmail());
        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && throwable.getMessage().contains("There is no authorities for this user"))
                .verify();
    }

    @Test
    @DisplayName("When find user by email and the authorities are empty return exception")
    void authoritiesEmptyReturnException() {
        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                emptyList(),
                LocalDateTime.now());

        when(applicationUserRepository.findByEmail(applicationUser.getEmail())).thenReturn(Mono.just(applicationUser));
        when(userAuthorityService.loadListOfSimpleGrantedAuthority(applicationUser)).thenReturn(Mono.empty());

        Mono<UserDetails> responseMono = applicationUserServiceImpl.findByUsername(applicationUser.getEmail());
        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && throwable.getMessage().contains("There is no authorities for this user"))
                .verify();
    }

    @Test
    @DisplayName("When get user by email that does not exist return exception")
    void getUserByEmailException() {
        when(applicationUserRepository.findByEmail(anyString())).thenReturn(Mono.empty());
        Mono<ApplicationUser> appUserMono = applicationUserServiceImpl.getUserByEmail("mail@mail.com");
        StepVerifier.create(appUserMono)
                .expectErrorMatches(throwable -> throwable instanceof UsernameNotFoundException
                        && throwable.getMessage().equals("User mail@mail.com does not exist"))
                .verify();
    }
}
