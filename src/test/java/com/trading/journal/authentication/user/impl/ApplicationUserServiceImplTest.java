package com.trading.journal.authentication.user.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.time.LocalDateTime;
import java.util.Collections;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.configuration.AuthoritiesHelper;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.user.Authority;

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
    ApplicationUserRepository repository;

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
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.singletonList(new Authority("ROLE_USER")),
                LocalDateTime.now());

        when(repository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(repository.existsByUserName(anyString())).thenReturn(Mono.just(false));
        when(encoder.encode(anyString())).thenReturn("sdsa54ds56a4ds564d");
        when(repository.save(any())).thenReturn(Mono.just(appUser));

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

        when(repository.existsByUserName(anyString())).thenReturn(Mono.just(true));
        when(repository.existsByEmail(anyString())).thenReturn(Mono.just(false));

        Mono<ApplicationUser> userMono = applicationUserServiceImpl.createNewUser(userRegistration);
        StepVerifier.create(userMono)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && ((ApplicationException) throwable).getStatusCode().equals(BAD_REQUEST)
                        && ((ApplicationException) throwable)
                                .getStatusText()
                                .equals("User name or email already exist"))
                .verify();

        verify(repository, never()).save(any());
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

        when(repository.existsByUserName(anyString())).thenReturn(Mono.just(false));
        when(repository.existsByEmail(anyString())).thenReturn(Mono.just(true));

        Mono<ApplicationUser> userMono = applicationUserServiceImpl.createNewUser(userRegistration);
        StepVerifier.create(userMono)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && ((ApplicationException) throwable).getStatusCode().equals(BAD_REQUEST)
                        && ((ApplicationException) throwable)
                                .getStatusText()
                                .equals("User name or email already exist"))
                .verify();

        verify(repository, never()).save(any());
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

        when(repository.existsByUserName(anyString())).thenReturn(Mono.just(true));
        when(repository.existsByEmail(anyString())).thenReturn(Mono.just(true));

        Mono<ApplicationUser> userMono = applicationUserServiceImpl.createNewUser(userRegistration);
        StepVerifier.create(userMono)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && ((ApplicationException) throwable).getStatusCode().equals(BAD_REQUEST)
                        && ((ApplicationException) throwable)
                                .getStatusText()
                                .equals("User name or email already exist"))
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("When validate new user with valid data return is valid")
    void isValid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(repository.existsByUserName(anyString())).thenReturn(Mono.just(false));
        when(repository.existsByEmail(anyString())).thenReturn(Mono.just(false));

        Mono<Boolean> userValid = applicationUserServiceImpl.validateNewUser(userName, email);

        StepVerifier.create(userValid).expectNext(true).verifyComplete();
    }

    @Test
    @DisplayName("When validate new user with invalid data return is invalid")
    void isInvalid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(repository.existsByUserName(anyString())).thenReturn(Mono.just(true));
        when(repository.existsByEmail(anyString())).thenReturn(Mono.just(false));

        Mono<Boolean> userValid = applicationUserServiceImpl.validateNewUser(userName, email);

        StepVerifier.create(userValid).expectNext(false).verifyComplete();
    }

    @Test
    @DisplayName("When validate new user with valid user name and invalid email return is valid")
    void isEmailInvalid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(repository.existsByUserName(anyString())).thenReturn(Mono.just(false));
        when(repository.existsByEmail(anyString())).thenReturn(Mono.just(true));

        Mono<Boolean> userValid = applicationUserServiceImpl.validateNewUser(userName, email);

        StepVerifier.create(userValid).expectNext(false).verifyComplete();
    }

    @Test
    @DisplayName("When validate new user with invalid user name and valid email return is valid")
    void isUserNameInvalid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(repository.existsByUserName(anyString())).thenReturn(Mono.just(true));
        when(repository.existsByEmail(anyString())).thenReturn(Mono.just(true));

        Mono<Boolean> userValid = applicationUserServiceImpl.validateNewUser(userName, email);

        StepVerifier.create(userValid).expectNext(false).verifyComplete();
    }

    @Test
    @DisplayName("When find user by email that exist return user details")
    void userDetails() {
        ApplicationUser appUser = new ApplicationUser(
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.singletonList(new Authority("ROLE_USER")),
                LocalDateTime.now());

        when(repository.findByEmail(appUser.email())).thenReturn(Mono.just(appUser));

        Mono<UserDetails> userDetailsMono = applicationUserServiceImpl.findByUsername(appUser.email());

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
        when(repository.findByEmail(anyString())).thenReturn(Mono.empty());

        Mono<UserDetails> responseMono = applicationUserServiceImpl.findByUsername("email@mail.com");
        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable -> throwable instanceof UsernameNotFoundException
                        && throwable.getMessage().equals("User email@mail.com does not exist"))
                .verify();
    }

    @Test
    @DisplayName("When find user by email and the authorities are null return exception")
    void authoritiesNullReturnException() {
        ApplicationUser appUser = new ApplicationUser(
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                null,
                LocalDateTime.now());

        when(repository.findByEmail(appUser.email())).thenReturn(Mono.just(appUser));

        Mono<UserDetails> responseMono = applicationUserServiceImpl.findByUsername(appUser.email());
        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && throwable.getMessage().contains("There is no authorities for this user"))
                .verify();
    }

    @Test
    @DisplayName("When find user by email and the authorities are empty return exception")
    void authoritiesEmptyReturnException() {
        ApplicationUser appUser = new ApplicationUser(
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.emptyList(),
                LocalDateTime.now());

        when(repository.findByEmail(appUser.email())).thenReturn(Mono.just(appUser));

        Mono<UserDetails> responseMono = applicationUserServiceImpl.findByUsername(appUser.email());
        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && throwable.getMessage().contains("There is no authorities for this user"))
                .verify();
    }

    @Test
    @DisplayName("When get user by email that does not exist return exception")
    void getUserByEmailException() {
        when(repository.findByEmail(anyString())).thenReturn(Mono.empty());
        Mono<ApplicationUser> appUserMono = applicationUserServiceImpl.getUserByEmail("mail@mail.com");
        StepVerifier.create(appUserMono)
                .expectErrorMatches(throwable -> throwable instanceof UsernameNotFoundException
                        && throwable.getMessage().equals("User mail@mail.com does not exist"))
                .verify();
    }
}
