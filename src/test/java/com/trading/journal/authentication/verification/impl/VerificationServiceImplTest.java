package com.trading.journal.authentication.verification.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationStatus;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.HashProvider;
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
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(SpringExtension.class)
class VerificationServiceImplTest {

    @Mock
    VerificationRepository verificationRepository;

    @Mock
    VerificationEmailService verificationEmailService;

    @Mock
    HashProvider hashProvider;

    @InjectMocks
    VerificationServiceImpl verificationService;

    @DisplayName("Given verification type REGISTRATION and application user send the verification to user email and never execute delete because previous verification did not exist")
    @Test
    void registrationVerification() {
        String hash = UUID.randomUUID().toString();
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
        when(hashProvider.generateHash(applicationUser.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(Mono.just(verificationSaved));
        when(verificationEmailService.sendEmail(any(), any())).thenReturn(Mono.empty());

        Mono<Void> voidMono = verificationService.send(VerificationType.REGISTRATION, applicationUser);
        StepVerifier.create(voidMono)
                .expectNextCount(0)
                .verifyComplete();
    }

    @DisplayName("Given verification type REGISTRATION and application user send the verification to user email and execute delete because previous verification did exist")
    @Test
    void registrationVerificationDeletePrevious() {
        String hash = UUID.randomUUID().toString();
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
        when(hashProvider.generateHash(applicationUser.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(Mono.just(verificationSaved));
        when(verificationEmailService.sendEmail(any(), any())).thenReturn(Mono.empty());

        Mono<Void> voidMono = verificationService.send(VerificationType.REGISTRATION, applicationUser);
        StepVerifier.create(voidMono)
                .expectNextCount(0)
                .verifyComplete();
    }

    @DisplayName("Given hash and email find current Verification")
    @Test
    void retrieve() {
        String hash = "12456";
        String email = "mail@mail.com";
        Verification verificationSaved = new Verification(1L,
                email,
                VerificationType.REGISTRATION,
                VerificationStatus.PENDING,
                hash,
                LocalDateTime.now());

        when(hashProvider.readHashValue(hash)).thenReturn(email);
        when(verificationRepository.getByHashAndEmail(hash, email)).thenReturn(Mono.just(verificationSaved));

        Mono<Verification> verificationMono = verificationService.retrieve(hash);

        StepVerifier.create(verificationMono)
                .expectNext(verificationSaved)
                .verifyComplete();
    }

    @DisplayName("Given hash and email find current Verification not found return exception")
    @Test
    void retrieveException() {
        String hash = "12456";
        String email = "mail@mail.com";
        when(hashProvider.readHashValue(hash)).thenReturn(email);
        when(verificationRepository.getByHashAndEmail(hash, email)).thenReturn(Mono.empty());

        Mono<Verification> verificationMono = verificationService.retrieve(hash);

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