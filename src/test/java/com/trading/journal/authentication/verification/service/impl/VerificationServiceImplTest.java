package com.trading.journal.authentication.verification.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.verification.*;
import com.trading.journal.authentication.verification.properties.VerificationProperties;
import com.trading.journal.authentication.verification.service.HashProvider;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
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
    UserRepository userRepository;

    @Mock
    HashProvider hashProvider;

    @Mock
    VerificationProperties verificationProperties;

    @InjectMocks
    VerificationServiceImpl verificationService;

    @DisplayName("Given verification type REGISTRATION and application user send the verification to user email and never execute delete because previous verification did not exist")
    @Test
    void registrationVerification() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L, "mail@mail.com", VerificationType.REGISTRATION, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(true).verified(true).createdAt(LocalDateTime.now()).authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")))).build();

        when(verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, user.getEmail())).thenReturn(Optional.empty());
        when(hashProvider.generateHash(user.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.REGISTRATION, user);
    }

    @DisplayName("Is verification REGISTRATION but email verification is not enabled then do not send")
    @Test
    void registrationVerificationNoSend() {
        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(true).verified(true).createdAt(LocalDateTime.now()).authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")))).build();

        when(verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, user.getEmail())).thenReturn(Optional.empty());
        when(verificationProperties.isEnabled()).thenReturn(false);

        verificationService.send(VerificationType.REGISTRATION, user);

        verify(hashProvider, never()).generateHash(anyString());
        verify(verificationRepository, never()).save(any());
        verify(verificationEmailService, never()).sendEmail(any(), any());
    }

    @DisplayName("Given verification type CHANGE_PASSWORD and application user send the verification to user email and never execute delete because previous verification did not exist")
    @Test
    void changePasswordVerification() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L, "mail@mail.com", VerificationType.CHANGE_PASSWORD, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(true).verified(true).createdAt(LocalDateTime.now()).authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")))).build();

        when(verificationRepository.getByTypeAndEmail(VerificationType.CHANGE_PASSWORD, user.getEmail())).thenReturn(Optional.empty());
        when(verificationProperties.isEnabled()).thenReturn(false);
        when(hashProvider.generateHash(user.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.CHANGE_PASSWORD, user);
    }

    @DisplayName("Given verification type ADMIN_REGISTRATION and application user send the verification to user email and never execute delete because previous verification did not exist")
    @Test
    void adminRegistrationVerification() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L, "mail@mail.com", VerificationType.ADMIN_REGISTRATION, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(true).verified(true).createdAt(LocalDateTime.now()).authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")))).build();

        when(verificationRepository.getByTypeAndEmail(VerificationType.ADMIN_REGISTRATION, user.getEmail())).thenReturn(Optional.empty());
        when(hashProvider.generateHash(user.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.ADMIN_REGISTRATION, user);
    }

    @DisplayName("Is verification ADMIN_REGISTRATION but email verification is not enabled then do not send")
    @Test
    void registrationAdminVerificationNoSend() {
        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(true).verified(true).createdAt(LocalDateTime.now()).authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")))).build();

        when(verificationRepository.getByTypeAndEmail(VerificationType.ADMIN_REGISTRATION, user.getEmail())).thenReturn(Optional.empty());
        when(verificationProperties.isEnabled()).thenReturn(false);

        verificationService.send(VerificationType.ADMIN_REGISTRATION, user);

        verify(hashProvider, never()).generateHash(anyString());
        verify(verificationRepository, never()).save(any());
        verify(verificationEmailService, never()).sendEmail(any(), any());
    }

    @DisplayName("Given verification type NEW_ORGANISATION_USER and application user send the verification to user email and never execute delete because previous verification did not exist")
    @Test
    void orgUserRegistrationVerification() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L, "mail@mail.com", VerificationType.NEW_ORGANISATION_USER, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(true).verified(true).createdAt(LocalDateTime.now()).authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")))).build();

        when(verificationRepository.getByTypeAndEmail(VerificationType.NEW_ORGANISATION_USER, user.getEmail())).thenReturn(Optional.empty());
        when(hashProvider.generateHash(user.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.NEW_ORGANISATION_USER, user);
    }

    @DisplayName("Given verification type DELETE_ME and application user send the verification to user email and never execute delete because previous verification did not exist")
    @Test
    void DELETE_MEVerification() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L, "mail@mail.com", VerificationType.DELETE_ME, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(true).verified(true).createdAt(LocalDateTime.now()).authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")))).build();

        when(verificationRepository.getByTypeAndEmail(VerificationType.DELETE_ME, user.getEmail())).thenReturn(Optional.empty());
        when(hashProvider.generateHash(user.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.DELETE_ME, user);
    }

    @DisplayName("Given verification type REGISTRATION and application user send the verification to user email and execute delete because previous verification did exist")
    @Test
    void registrationVerificationDeletePrevious() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L, "mail@mail.com", VerificationType.REGISTRATION, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(true).verified(true).createdAt(LocalDateTime.now()).authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")))).build();

        when(verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, user.getEmail())).thenReturn(Optional.of(verificationSaved));
        when(verificationProperties.isEnabled()).thenReturn(true);
        when(hashProvider.generateHash(user.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.REGISTRATION, user);
    }

    @DisplayName("Given verification type DELETE_ME and application user send the verification to user email and execute delete because previous verification did exist")
    @Test
    void DELETE_MEVerificationDeletePrevious() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L, "mail@mail.com", VerificationType.DELETE_ME, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(true).verified(true).createdAt(LocalDateTime.now()).authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")))).build();

        when(verificationRepository.getByTypeAndEmail(VerificationType.DELETE_ME, user.getEmail())).thenReturn(Optional.of(verificationSaved));
        when(verificationProperties.isEnabled()).thenReturn(true);
        when(hashProvider.generateHash(user.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.DELETE_ME, user);
    }

    @DisplayName("Given verification type CHANGE_PASSWORD and application user send the verification to user email and execute delete because previous verification did exist")
    @Test
    void changePasswordVerificationDeletePrevious() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L, "mail@mail.com", VerificationType.CHANGE_PASSWORD, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(true).verified(true).createdAt(LocalDateTime.now()).authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")))).build();

        when(verificationRepository.getByTypeAndEmail(VerificationType.CHANGE_PASSWORD, user.getEmail())).thenReturn(Optional.of(verificationSaved));
        when(hashProvider.generateHash(user.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.CHANGE_PASSWORD, user);
    }

    @DisplayName("Given verification type ADMIN_REGISTRATION and application user send the verification to user email and execute delete because previous verification did exist")
    @Test
    void adminRegistrationVerificationDeletePrevious() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L, "mail@mail.com", VerificationType.ADMIN_REGISTRATION, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(true).verified(true).createdAt(LocalDateTime.now()).authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")))).build();

        when(verificationRepository.getByTypeAndEmail(VerificationType.ADMIN_REGISTRATION, user.getEmail())).thenReturn(Optional.of(verificationSaved));
        when(hashProvider.generateHash(user.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.ADMIN_REGISTRATION, user);
    }

    @DisplayName("Given verification type NEW_ORGANISATION_USER and application user send the verification to user email and execute delete because previous verification did exist")
    @Test
    void orgUserVerificationDeletePrevious() {
        String hash = UUID.randomUUID().toString();
        Verification verificationSaved = new Verification(1L, "mail@mail.com", VerificationType.NEW_ORGANISATION_USER, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(true).verified(true).createdAt(LocalDateTime.now()).authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")))).build();

        when(verificationRepository.getByTypeAndEmail(VerificationType.NEW_ORGANISATION_USER, user.getEmail())).thenReturn(Optional.of(verificationSaved));
        when(hashProvider.generateHash(user.getEmail())).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());

        verificationService.send(VerificationType.NEW_ORGANISATION_USER, user);
    }

    @DisplayName("Given hash and email find current Verification REGISTRATION")
    @Test
    void retrieveRegistration() {
        String hash = "12456";
        String email = "mail@mail.com";
        Verification verificationSaved = new Verification(1L, email, VerificationType.REGISTRATION, VerificationStatus.PENDING, hash, LocalDateTime.now());

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
        Verification verificationSaved = new Verification(1L, email, VerificationType.CHANGE_PASSWORD, VerificationStatus.PENDING, hash, LocalDateTime.now());

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
        Verification verificationSaved = new Verification(1L, email, VerificationType.ADMIN_REGISTRATION, VerificationStatus.PENDING, hash, LocalDateTime.now());

        when(hashProvider.readHashValue(hash)).thenReturn(email);
        when(verificationRepository.getByHashAndEmail(hash, email)).thenReturn(Optional.of(verificationSaved));

        Verification verification = verificationService.retrieve(hash);

        assertThat(verificationSaved).isEqualTo(verification);
    }

    @DisplayName("Given hash and email find current Verification NEW_ORGANISATION_USER")
    @Test
    void retrieveNEW_ORGANISATION_USER() {
        String hash = "12456";
        String email = "mail@mail.com";
        Verification verificationSaved = new Verification(1L, email, VerificationType.NEW_ORGANISATION_USER, VerificationStatus.PENDING, hash, LocalDateTime.now());

        when(hashProvider.readHashValue(hash)).thenReturn(email);
        when(verificationRepository.getByHashAndEmail(hash, email)).thenReturn(Optional.of(verificationSaved));

        Verification verification = verificationService.retrieve(hash);

        assertThat(verificationSaved).isEqualTo(verification);
    }

    @DisplayName("Given hash and email find current Verification DELETE_ME")
    @Test
    void retrieveDELETE_ME() {
        String hash = "12456";
        String email = "mail@mail.com";
        Verification verificationSaved = new Verification(1L, email, VerificationType.DELETE_ME, VerificationStatus.PENDING, hash, LocalDateTime.now());

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
        Verification verificationSaved = new Verification(1L, "mail@mail.com", VerificationType.REGISTRATION, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        doNothing().when(verificationRepository).delete(verificationSaved);

        verificationService.verify(verificationSaved);

        verify(userRepository, never()).findByEmail(anyString());
        verify(hashProvider, never()).generateHash(anyString());
        verify(verificationRepository, never()).save(any());
        verify(verificationEmailService, never()).sendEmail(any(), any());
    }

    @DisplayName("Given Verification CHANGE_PASSWORD delete it when Verify")
    @Test
    void deleteChangePassword() {
        Verification verificationSaved = new Verification(1L, "mail@mail.com", VerificationType.CHANGE_PASSWORD, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        doNothing().when(verificationRepository).delete(verificationSaved);

        verificationService.verify(verificationSaved);

        verify(userRepository, never()).findByEmail(anyString());
        verify(hashProvider, never()).generateHash(anyString());
        verify(verificationRepository, never()).save(any());
        verify(verificationEmailService, never()).sendEmail(any(), any());
    }

    @DisplayName("Given Verification DELETE_ME delete it when Verify")
    @Test
    void deleteDELETE_ME() {
        Verification verificationSaved = new Verification(1L, "mail@mail.com", VerificationType.DELETE_ME, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        doNothing().when(verificationRepository).delete(verificationSaved);

        verificationService.verify(verificationSaved);

        verify(userRepository, never()).findByEmail(anyString());
        verify(hashProvider, never()).generateHash(anyString());
        verify(verificationRepository, never()).save(any());
        verify(verificationEmailService, never()).sendEmail(any(), any());
    }

    @DisplayName("Given Verification ADMIN_REGISTRATION send a CHANGE_PASSWORD request and delete ADMIN_REGISTRATION")
    @Test
    void verifyAndDeleteAdminRegistration() {
        String hash = UUID.randomUUID().toString();
        String email = "mail@mail.com";

        Verification adminRegistration = new Verification(1L, email, VerificationType.ADMIN_REGISTRATION, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        Verification changerPassword = new Verification(1L, email, VerificationType.CHANGE_PASSWORD, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(false).verified(false).createdAt(LocalDateTime.now()).authorities(emptyList()).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(verificationRepository.getByTypeAndEmail(VerificationType.CHANGE_PASSWORD, email)).thenReturn(Optional.empty());
        when(hashProvider.generateHash(email)).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(changerPassword);
        doNothing().when(verificationEmailService).sendEmail(any(), any());
        doNothing().when(verificationRepository).delete(adminRegistration);

        verificationService.verify(adminRegistration);
    }

    @DisplayName("Given Verification NEW_ORGANISATION_USER send a CHANGE_PASSWORD request and delete ADMIN_REGISTRATION")
    @Test
    void verifyAndDeleteNEW_ORGANISATION_USER() {
        String hash = UUID.randomUUID().toString();
        String email = "mail@mail.com";

        Verification adminRegistration = new Verification(1L, email, VerificationType.NEW_ORGANISATION_USER, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        Verification changerPassword = new Verification(1L, email, VerificationType.CHANGE_PASSWORD, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email("mail@mail.com").enabled(false).verified(false).createdAt(LocalDateTime.now()).authorities(emptyList()).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(verificationRepository.getByTypeAndEmail(VerificationType.CHANGE_PASSWORD, email)).thenReturn(Optional.empty());
        when(hashProvider.generateHash(email)).thenReturn(hash);
        when(verificationRepository.save(any())).thenReturn(changerPassword);
        doNothing().when(verificationEmailService).sendEmail(any(), any());
        doNothing().when(verificationRepository).delete(adminRegistration);

        verificationService.verify(adminRegistration);
    }

    @DisplayName("Get all verifications by email")
    @Test
    void getByEmail() {
        String email = "mail@mail.com";

        List<Verification> verifications = asList(new Verification(1L, email, VerificationType.REGISTRATION, VerificationStatus.ERROR, "12456", LocalDateTime.now()), new Verification(1L, email, VerificationType.NEW_ORGANISATION_USER, VerificationStatus.PENDING, "12456", LocalDateTime.now()));

        when(verificationRepository.getByEmail(email)).thenReturn(verifications);

        List<Verification> byEmail = verificationService.getByEmail(email);
        assertThat(byEmail).isEqualTo(verifications);
    }

    @DisplayName("Get all verifications by email not found return empty list")
    @Test
    void getByEmailEmpty() {
        String email = "mail@mail.com";

        when(verificationRepository.getByEmail(email)).thenReturn(emptyList());

        List<Verification> byEmail = verificationService.getByEmail(email);
        assertThat(byEmail).isEmpty();
    }

    @DisplayName("Create new verification given Type and Email")
    @Test
    void createByEmail() {
        String email = "mail@mail.com";

        User user = User.builder().id(1L).password("password").firstName("lastName").lastName("Wick").email(email).enabled(true).verified(true).createdAt(LocalDateTime.now()).authorities(emptyList()).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Verification verificationSaved = new Verification(1L, email, VerificationType.NEW_ORGANISATION_USER, VerificationStatus.PENDING, "12456", LocalDateTime.now());

        when(verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, email)).thenReturn(Optional.empty());
        when(hashProvider.generateHash(user.getEmail())).thenReturn("12456");
        when(verificationRepository.save(any())).thenReturn(verificationSaved);
        doNothing().when(verificationEmailService).sendEmail(any(), any());
        when(verificationRepository.getByTypeAndEmail(VerificationType.REGISTRATION, email)).thenReturn(Optional.of(verificationSaved));

        VerificationRequest request = new VerificationRequest(email, VerificationType.REGISTRATION);
        Verification verification = verificationService.create(request);
        assertThat(verification).isEqualTo(verificationSaved);

        verify(verificationRepository, times(2)).getByTypeAndEmail(VerificationType.REGISTRATION, email);
    }

    @DisplayName("Create new verification given Type and Email, email not found return exception")
    @Test
    void createByEmailException() {
        String email = "mail@mail.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        VerificationRequest request = new VerificationRequest(email, VerificationType.REGISTRATION);
        ApplicationException exception = assertThrows(ApplicationException.class, () -> verificationService.create(request));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("User mail@mail.com does not exist");

        verify(verificationRepository, never()).getByTypeAndEmail(any(), any());
        verify(hashProvider, never()).generateHash(anyString());
        verify(verificationRepository, never()).save(any());
        verify(verificationEmailService, never()).sendEmail(any(), any());
    }
}