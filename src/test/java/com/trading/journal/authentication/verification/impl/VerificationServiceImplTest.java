package com.trading.journal.authentication.verification.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.verification.*;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import com.trading.journal.authentication.verification.service.VerificationRepository;
import com.trading.journal.authentication.verification.service.impl.VerificationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(SpringExtension.class)
class VerificationServiceImplTest {

    @Mock
    VerificationRepository verificationRepository;

    @Mock
    VerificationEmailService verificationEmailService;

    @InjectMocks
    VerificationServiceImpl verificationService;

    @DisplayName("Given verification type REGISTRATION and application user send the verification to user email and never execute delete because previous verification did not exist")
    @Test
    void registrationVerification() {
        Verification verificationToSave = Verification.builder()
                .email("mail@mail.com")
                .hash("12456")
                .status(VerificationStatus.PENDING)
                .type(VerificationType.REGISTRATION)
                .build();

        Verification verificationSaved = new Verification(1L,
                "mail@mail.com",
                VerificationType.REGISTRATION,
                VerificationStatus.PENDING,
                "12456",
                LocalDateTime.now());

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserAdm",
                "123456",
                "User",
                "Admin",
                "mail@mail.com",
                true,
                true,
                Collections.singletonList(new UserAuthority(1L, 1L, 1L, "ROLE_USER")),
                LocalDateTime.now());

        when(verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, applicationUser.getEmail())).thenReturn(Mono.empty());
        when(verificationRepository.save(verificationToSave)).thenReturn(Mono.just(verificationSaved));
        when(verificationEmailService.sendEmail(verificationSaved, applicationUser)).thenReturn(Mono.empty());

        Mono<Void> voidMono = verificationService.send(VerificationType.REGISTRATION, applicationUser);
        StepVerifier.create(voidMono)
                .expectNextCount(0)
                .verifyComplete();

        verify(verificationRepository, never()).delete(any());
    }

    @DisplayName("Given verification type REGISTRATION and application user send the verification to user email and execute delete because previous verification did exist")
    @Test
    void registrationVerificationDeletePrevious() {
        Verification verificationToSave = Verification.builder()
                .email("mail@mail.com")
                .hash("12456")
                .status(VerificationStatus.PENDING)
                .type(VerificationType.REGISTRATION)
                .build();

        Verification verificationSaved = new Verification(1L,
                "mail@mail.com",
                VerificationType.REGISTRATION,
                VerificationStatus.PENDING,
                "12456",
                LocalDateTime.now());

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserAdm",
                "123456",
                "User",
                "Admin",
                "mail@mail.com",
                true,
                true,
                Collections.singletonList(new UserAuthority(1L, 1L, 1L, "ROLE_USER")),
                LocalDateTime.now());

        when(verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, applicationUser.getEmail())).thenReturn(Mono.just(verificationSaved));
        when(verificationRepository.delete(verificationSaved)).thenReturn(Mono.empty());
        when(verificationRepository.save(verificationToSave)).thenReturn(Mono.just(verificationSaved));
        when(verificationEmailService.sendEmail(verificationSaved, applicationUser)).thenReturn(Mono.empty());

        Mono<Void> voidMono = verificationService.send(VerificationType.REGISTRATION, applicationUser);
        StepVerifier.create(voidMono)
                .expectNextCount(0)
                .verifyComplete();

        verify(verificationRepository).delete(any());
    }

    @DisplayName("Given hash and email find current Verification")
    @Test
    void retrieve() {
        Verification verificationSaved = new Verification(1L,
                "mail@mail.com",
                VerificationType.REGISTRATION,
                VerificationStatus.PENDING,
                "12456",
                LocalDateTime.now());

        when(verificationRepository.getByHashAndEmail("12456", "mail@mail.com")).thenReturn(Mono.just(verificationSaved));

        Mono<Verification> verificationMono = verificationService.retrieve("12456", "mail@mail.com");

        StepVerifier.create(verificationMono)
                .expectNext(verificationSaved)
                .verifyComplete();
    }

    @DisplayName("Given hash and email find current Verification not found return exception")
    @Test
    void retrieveException() {
        when(verificationRepository.getByHashAndEmail("12456", "mail@mail.com")).thenReturn(Mono.empty());

        Mono<Verification> verificationMono = verificationService.retrieve("12456", "mail@mail.com");

        StepVerifier.create(verificationMono)
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException
                        && ((ApplicationException) throwable).getStatusCode().equals(BAD_REQUEST)
                        && ((ApplicationException) throwable)
                        .getStatusText()
                        .equals("Request is invalid"))
                .verify();
    }

    @DisplayName("Given Verification delete it when Verify")
    @Test
    void delete() {
        Verification verificationSaved = new Verification(1L,
                "mail@mail.com",
                VerificationType.REGISTRATION,
                VerificationStatus.PENDING,
                "12456",
                LocalDateTime.now());

        when(verificationRepository.delete(verificationSaved)).thenReturn(Mono.empty());

        Mono<Void> voidMono = verificationService.verify(verificationSaved);

        StepVerifier.create(voidMono)
                .expectNextCount(0)
                .verifyComplete();
    }
}