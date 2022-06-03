package com.trading.journal.authentication.registration.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.authority.UserAuthority;

import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationStatus;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.properties.VerificationProperties;
import com.trading.journal.authentication.verification.service.VerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class RegistrationServiceImplTest {

    @Mock
    ApplicationUserService applicationUserService;

    @Mock
    VerificationService verificationService;

    @Mock
    VerificationProperties verificationProperties;

    @InjectMocks
    RegistrationServiceImpl registrationService;

    @Test
    @DisplayName("When registry a user and it is enabled, don not send verification email")
    void registryWithoutVerification() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.singletonList(new UserAuthority(1L, 1L, 1L, "ROLE_USER")),
                LocalDateTime.now());

        when(applicationUserService.createNewUser(userRegistration)).thenReturn(Mono.just(applicationUser));
        when(verificationProperties.isEnabled()).thenReturn(false);

        Mono<SignUpResponse> responseMono = registrationService.signUp(userRegistration);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.email()).isEqualTo("mail@mail.com");
                    assertThat(response.enabled()).isTrue();
                })
                .verifyComplete();

        verify(verificationService, never()).send(any(), any());
    }

    @Test
    @DisplayName("When registry a user and it is not enabled, send verification email")
    void registryWithVerification() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                "mail@mail.com",
                false,
                false,
                Collections.singletonList(new UserAuthority(1L, 1L, 1L, "ROLE_USER")),
                LocalDateTime.now());

        when(applicationUserService.createNewUser(userRegistration)).thenReturn(Mono.just(applicationUser));
        when(verificationProperties.isEnabled()).thenReturn(true);
        when(verificationService.send(VerificationType.REGISTRATION, applicationUser)).thenReturn(Mono.empty());

        Mono<SignUpResponse> responseMono = registrationService.signUp(userRegistration);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.email()).isEqualTo("mail@mail.com");
                    assertThat(response.enabled()).isFalse();
                })
                .verifyComplete();

        verify(verificationService).send(any(), any());
    }

    @Test
    @DisplayName("Process email verification")
    void emailVerification() {
        String hash = UUID.randomUUID().toString();
        String email = "mail@mail.com";
        Verification verification = new Verification(1L,
                email,
                VerificationType.REGISTRATION,
                VerificationStatus.PENDING,
                hash,
                LocalDateTime.now());

        when(verificationService.retrieve(hash)).thenReturn(Mono.just(verification));
        when(applicationUserService.verifyNewUser(verification.getEmail())).thenReturn(Mono.empty());
        when(verificationService.verify(verification)).thenReturn(Mono.empty());

        Mono<Void> voidMono = registrationService.verify(hash);
        StepVerifier.create(voidMono)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("Process email verification return exception when retrieving the hash values do not execute all process")
    void emailVerificationError() {
        String hash = UUID.randomUUID().toString();
        when(verificationService.retrieve(hash)).thenReturn(Mono.error(new ApplicationException("any error message")));

        Mono<Void> voidMono = registrationService.verify(hash);
        StepVerifier.create(voidMono)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && throwable.getMessage().contains("any error message"))
                .verify();

        verify(applicationUserService, never()).verifyNewUser(any());
        verify(verificationService, never()).verify(any());
    }

    @Test
    @DisplayName("Send new email verification")
    void newEmailVerification() {
        String email = "mail@mail.com";

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                email,
                false,
                false,
                Collections.singletonList(new UserAuthority(1L, 1L, 1L, "ROLE_USER")),
                LocalDateTime.now());

        when(verificationProperties.isEnabled()).thenReturn(true);
        when(applicationUserService.getUserByEmail(email)).thenReturn(Mono.just(applicationUser));
        when(verificationService.send(VerificationType.REGISTRATION, applicationUser)).thenReturn(Mono.empty());

        Mono<SignUpResponse> responseMono = registrationService.sendVerification(email);
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.email()).isEqualTo(email);
                    assertThat(response.enabled()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Send new email verification when verification process is disabled, do dot send verification email")
    void newEmailVerificationDisabled() {
        String email = "mail@mail.com";
        when(verificationProperties.isEnabled()).thenReturn(false);

        Mono<SignUpResponse> responseMono = registrationService.sendVerification(email);
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.email()).isEqualTo(email);
                    assertThat(response.enabled()).isTrue();
                })
                .verifyComplete();

        verify(applicationUserService, never()).getUserByEmail(anyString());
        verify(verificationService, never()).send(any(), any());
    }

    @Test
    @DisplayName("Send new email verification when user is already enabled, do dot send verification email")
    void newEmailVerificationUserEnabled() {
        String email = "mail@mail.com";

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                email,
                true,
                true,
                Collections.singletonList(new UserAuthority(1L, 1L, 1L, "ROLE_USER")),
                LocalDateTime.now());

        when(verificationProperties.isEnabled()).thenReturn(true);
        when(applicationUserService.getUserByEmail(email)).thenReturn(Mono.just(applicationUser));

        Mono<SignUpResponse> responseMono = registrationService.sendVerification(email);
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.email()).isEqualTo(email);
                    assertThat(response.enabled()).isTrue();
                })
                .verifyComplete();

        verify(verificationService, never()).send(any(), any());
    }

    @Test
    @DisplayName("Send new email verification throws exception because user does not exist")
    void newEmailVerificationException() {
        String email = "mail@mail.com";

        when(verificationProperties.isEnabled()).thenReturn(true);
        when(applicationUserService.getUserByEmail(email)).thenReturn(Mono.empty());

        Mono<SignUpResponse> responseMono = registrationService.sendVerification(email);
        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable -> throwable instanceof UsernameNotFoundException
                        && throwable.getMessage().contains(String.format("User %s does not exist", email)))
                .verify();

        verify(verificationService, never()).send(any(), any());
    }
}
