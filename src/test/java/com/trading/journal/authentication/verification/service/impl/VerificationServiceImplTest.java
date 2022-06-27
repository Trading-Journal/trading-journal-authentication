package com.trading.journal.authentication.verification.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationStatus;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.HashProvider;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import com.trading.journal.authentication.verification.VerificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(SpringExtension.class)
class VerificationServiceImplTest {

    @Mock
    VerificationRepository verificationRepository;

    @Mock
    VerificationEmailService verificationEmailService;

    @Mock
    ApplicationUserService applicationUserService;

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
                Collections.singletonList(new UserAuthority(null,"ROLE_USER", 1L)),
                LocalDateTime.now());

        when(verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, applicationUser.getEmail())).thenReturn(Optional.empty());
        when(hashProvider.generateHash(applicationUser.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.REGISTRATION, applicationUser);
    }

    @DisplayName("Given verification type CHANGE_PASSWORD and application user send the verification to user email and never execute delete because previous verification did not exist")
    @Test
    void changePasswordVerification() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L,
                "mail@mail.com",
                VerificationType.CHANGE_PASSWORD,
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
                Collections.singletonList(new UserAuthority(null,"ROLE_USER", 1L)),
                LocalDateTime.now());

        when(verificationRepository.getByTypeAndEmail(VerificationType.CHANGE_PASSWORD, applicationUser.getEmail())).thenReturn(Optional.empty());
        when(hashProvider.generateHash(applicationUser.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.CHANGE_PASSWORD, applicationUser);
    }

    @DisplayName("Given verification type ADMIN_REGISTRATION and application user send the verification to user email and never execute delete because previous verification did not exist")
    @Test
    void adminRegistrationVerification() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L,
                "mail@mail.com",
                VerificationType.ADMIN_REGISTRATION,
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
                Collections.singletonList(new UserAuthority(null,"ROLE_USER", 1L)),
                LocalDateTime.now());

        when(verificationRepository.getByTypeAndEmail(VerificationType.ADMIN_REGISTRATION, applicationUser.getEmail())).thenReturn(Optional.empty());
        when(hashProvider.generateHash(applicationUser.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.ADMIN_REGISTRATION, applicationUser);
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
                Collections.singletonList(new UserAuthority(null,"ROLE_USER", 1L)),
                LocalDateTime.now());

        when(verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, applicationUser.getEmail())).thenReturn(Optional.of(verificationSaved));
        when(hashProvider.generateHash(applicationUser.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.REGISTRATION, applicationUser);
    }

    @DisplayName("Given verification type CHANGE_PASSWORD and application user send the verification to user email and execute delete because previous verification did exist")
    @Test
    void changePasswordVerificationDeletePrevious() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L,
                "mail@mail.com",
                VerificationType.CHANGE_PASSWORD,
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
                Collections.singletonList(new UserAuthority(null,"ROLE_USER", 1L)),
                LocalDateTime.now());

        when(verificationRepository.getByTypeAndEmail(VerificationType.CHANGE_PASSWORD, applicationUser.getEmail())).thenReturn(Optional.of(verificationSaved));
        when(hashProvider.generateHash(applicationUser.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.CHANGE_PASSWORD, applicationUser);
    }

    @DisplayName("Given verification type ADMIN_REGISTRATION and application user send the verification to user email and execute delete because previous verification did exist")
    @Test
    void adminRegistrationVerificationDeletePrevious() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L,
                "mail@mail.com",
                VerificationType.ADMIN_REGISTRATION,
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
                Collections.singletonList(new UserAuthority(null,"ROLE_USER", 1L)),
                LocalDateTime.now());

        when(verificationRepository.getByTypeAndEmail(VerificationType.ADMIN_REGISTRATION, applicationUser.getEmail())).thenReturn(Optional.of(verificationSaved));
        when(hashProvider.generateHash(applicationUser.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.ADMIN_REGISTRATION, applicationUser);
    }

    @DisplayName("Given hash and email find current Verification REGISTRATION")
    @Test
    void retrieveRegistration() {
        String hash = "12456";
        String email = "mail@mail.com";
        Verification verificationSaved = new Verification(1L,
                email,
                VerificationType.REGISTRATION,
                VerificationStatus.PENDING,
                hash,
                LocalDateTime.now());

        when(hashProvider.readHashValue(hash)).thenReturn(email);
        when(verificationRepository.getByHashAndEmail(hash, email)).thenReturn(Optional.of(verificationSaved));

        Verification verification = verificationService.retrieve(hash);
        assertThat(verificationSaved).isEqualTo(verification);
    }

    @DisplayName("Given hash and email find current Verification CHANGE_PASSWORD")
    @Test
    void retrieveChangePassword() {
        String hash = "12456";
        String email = "mail@mail.com";
        Verification verificationSaved = new Verification(1L,
                email,
                VerificationType.CHANGE_PASSWORD,
                VerificationStatus.PENDING,
                hash,
                LocalDateTime.now());

        when(hashProvider.readHashValue(hash)).thenReturn(email);
        when(verificationRepository.getByHashAndEmail(hash, email)).thenReturn(Optional.of(verificationSaved));

        Verification verification = verificationService.retrieve(hash);

        assertThat(verificationSaved).isEqualTo(verification);
    }

    @DisplayName("Given hash and email find current Verification ADMIN_REGISTRATION")
    @Test
    void retrieveAdminRegistration() {
        String hash = "12456";
        String email = "mail@mail.com";
        Verification verificationSaved = new Verification(1L,
                email,
                VerificationType.ADMIN_REGISTRATION,
                VerificationStatus.PENDING,
                hash,
                LocalDateTime.now());

        when(hashProvider.readHashValue(hash)).thenReturn(email);
        when(verificationRepository.getByHashAndEmail(hash, email)).thenReturn(Optional.of(verificationSaved));

        Verification verification = verificationService.retrieve(hash);

        assertThat(verificationSaved).isEqualTo(verification);
    }

    @DisplayName("Given hash and email find current Verification not found return exception")
    @Test
    void retrieveException() {
        String hash = "12456";
        String email = "mail@mail.com";
        when(hashProvider.readHashValue(hash)).thenReturn(email);
        when(verificationRepository.getByHashAndEmail(hash, email)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> verificationService.retrieve(hash));
        assertThat(exception.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Request is invalid");
    }

    @DisplayName("Given Verification REGISTRATION delete it when Verify")
    @Test
    void deleteRegistration() {
        Verification verificationSaved = new Verification(1L,
                "mail@mail.com",
                VerificationType.REGISTRATION,
                VerificationStatus.PENDING,
                "12456",
                LocalDateTime.now());

        doNothing().when(verificationRepository).delete(verificationSaved);

        verificationService.verify(verificationSaved);

        verify(applicationUserService, never()).getUserByEmail(anyString());
        verify(hashProvider, never()).generateHash(anyString());
        verify(verificationRepository, never()).save(any());
        verify(verificationEmailService, never()).sendEmail(any(), any());
    }

    @DisplayName("Given Verification CHANGE_PASSWORD delete it when Verify")
    @Test
    void deleteChangePassword() {
        Verification verificationSaved = new Verification(1L,
                "mail@mail.com",
                VerificationType.CHANGE_PASSWORD,
                VerificationStatus.PENDING,
                "12456",
                LocalDateTime.now());

        doNothing().when(verificationRepository).delete(verificationSaved);

        verificationService.verify(verificationSaved);

        verify(applicationUserService, never()).getUserByEmail(anyString());
        verify(hashProvider, never()).generateHash(anyString());
        verify(verificationRepository, never()).save(any());
        verify(verificationEmailService, never()).sendEmail(any(), any());
    }

    @DisplayName("Given Verification ADMIN_REGISTRATION delete it when Verify")
    @Test
    void verifyAndDeleteAdminRegistration() {
        String hash = UUID.randomUUID().toString();
        String email = "mail@mail.com";

        Verification adminRegistration = new Verification(1L,
                email,
                VerificationType.ADMIN_REGISTRATION,
                VerificationStatus.PENDING,
                "12456",
                LocalDateTime.now());

        Verification changerPassword = new Verification(1L,
                email,
                VerificationType.CHANGE_PASSWORD,
                VerificationStatus.PENDING,
                "12456",
                LocalDateTime.now());

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserAdm",
                "123456",
                "User",
                "Admin",
                email,
                false,
                false,
                emptyList(),
                LocalDateTime.now());

        when(applicationUserService.getUserByEmail(email)).thenReturn(applicationUser);
        when(verificationRepository.getByTypeAndEmail(VerificationType.CHANGE_PASSWORD, email)).thenReturn(Optional.empty());
        when(hashProvider.generateHash(email)).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(changerPassword);
        doNothing().when(verificationEmailService).sendEmail(any(), any());
        doNothing().when(verificationRepository).delete(adminRegistration);

        verificationService.verify(adminRegistration);
    }
}