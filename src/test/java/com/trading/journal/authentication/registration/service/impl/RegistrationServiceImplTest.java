package com.trading.journal.authentication.registration.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;

import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.authority.UserAuthority;

import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class RegistrationServiceImplTest {

    @Mock
    ApplicationUserService applicationUserService;

    @Mock
    VerificationService verificationService;

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
                Collections.singletonList(new UserAuthority(1L, 1L, 1L,"ROLE_USER")),
                LocalDateTime.now());

        when(applicationUserService.createNewUser(userRegistration)).thenReturn(Mono.just(applicationUser));

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
                Collections.singletonList(new UserAuthority(1L, 1L, 1L,"ROLE_USER")),
                LocalDateTime.now());

        when(applicationUserService.createNewUser(userRegistration)).thenReturn(Mono.just(applicationUser));
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
}
