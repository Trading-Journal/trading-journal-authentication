package com.trading.journal.authentication.password.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.email.EmailField;
import com.trading.journal.authentication.email.EmailRequest;
import com.trading.journal.authentication.email.service.EmailSender;
import com.trading.journal.authentication.password.ChangePassword;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.service.UserService;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationStatus;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class PasswordManagementServiceImplTest {

    @Mock
    UserService userService;

    @Mock
    VerificationService verificationService;

    @Mock
    EmailSender emailSender;

    @InjectMocks
    PasswordManagementServiceImpl passwordService;

    @DisplayName("Request for a password change")
    @Test
    void passwordChangeRequest() {
        String email = "mail@mail.com";
        User user = User.builder()
                .id(1L)
                .userName("UserName")
                .password("encoded_password")
                .firstName("lastName")
                .lastName("Wick")
                .email(email)
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(Collections.singletonList(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))))
                .build();

        when(userService.getUserByEmail(email)).thenReturn(Optional.of(user));
        doNothing().when(verificationService).send(VerificationType.CHANGE_PASSWORD, user);

        passwordService.requestPasswordChange(email);

        verify(userService).unprovenUser(email);
    }

    @DisplayName("Password change hash not found return exception")
    @Test
    void passwordChangeHashNotFound() {
        ChangePassword changePassword = new ChangePassword("mail@email.com", "null", "dad231#$#4", "dad231#$#4123");

        when(verificationService.retrieve(changePassword.hash())).thenReturn(null);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> passwordService.changePassword(changePassword));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Change password request is invalid");

        verify(userService, never()).getUserByEmail(anyString());
        verify(emailSender, never()).send(any());
        verify(verificationService, never()).verify(any());
        verify(userService, never()).verifyUser(anyString());
    }

    @DisplayName("Password change hash email and change request email are different return exception")
    @Test
    void passwordChangeHashDifferentEmail() {
        String email = "mail@email.com";
        String hash = UUID.randomUUID().toString();

        ChangePassword changePassword = new ChangePassword(email, hash, "dad231#$#4", "dad231#$#4123");
        Verification verification = new Verification(1L, "anotheremail@mail.com", VerificationType.CHANGE_PASSWORD, VerificationStatus.PENDING, hash, LocalDateTime.now());

        when(verificationService.retrieve(changePassword.hash())).thenReturn(verification);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> passwordService.changePassword(changePassword));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Change password request is invalid");

        verify(userService, never()).getUserByEmail(anyString());
        verify(emailSender, never()).send(any());
        verify(verificationService, never()).verify(any());
        verify(userService, never()).verifyUser(anyString());
    }

    @DisplayName("Password change verification is not CHANGE_PASSWORD return exception")
    @Test
    void passwordChangeNotCHANGE_PASSWORD() {
        String email = "mail@email.com";
        String hash = UUID.randomUUID().toString();

        ChangePassword changePassword = new ChangePassword(email, hash, "dad231#$#4", "dad231#$#4123");
        Verification verification = new Verification(1L, email, VerificationType.REGISTRATION, VerificationStatus.PENDING, hash, LocalDateTime.now());

        when(verificationService.retrieve(changePassword.hash())).thenReturn(verification);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> passwordService.changePassword(changePassword));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Change password request is invalid");

        verify(userService, never()).getUserByEmail(anyString());
        verify(emailSender, never()).send(any());
        verify(verificationService, never()).verify(any());
        verify(userService, never()).verifyUser(anyString());
    }

    @DisplayName("Password change verification executed successfully")
    @Test
    void passwordChange() {
        String email = "mail@email.com";
        String hash = UUID.randomUUID().toString();
        User user = User.builder()
                .id(1L)
                .userName("UserName")
                .password("encoded_password")
                .firstName("lastName")
                .lastName("Wick")
                .email(email)
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        EmailRequest emailRequest = new EmailRequest(
                "Confirmação de alteração senha",
                "mail/change-password-confirmation.html",
                singletonList(new EmailField("$NAME", "firstName lastName")),
                singletonList(user.getEmail()));

        ChangePassword changePassword = new ChangePassword(email, hash, "dad231#$#4", "dad231#$#4123");
        Verification verification = new Verification(1L, email, VerificationType.CHANGE_PASSWORD, VerificationStatus.PENDING, hash, LocalDateTime.now());

        when(verificationService.retrieve(changePassword.hash())).thenReturn(verification);
        when(userService.changePassword(email, "dad231#$#4")).thenReturn(user);
        when(userService.getUserByEmail(changePassword.email())).thenReturn(Optional.of(user));
        doNothing().when(emailSender).send(emailRequest);
        doNothing().when(verificationService).verify(verification);

        passwordService.changePassword(changePassword);

        verify(userService).verifyUser(email);
    }
}