package com.trading.journal.authentication.authentication.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authentication.ChangePassword;
import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.email.EmailField;
import com.trading.journal.authentication.email.EmailRequest;
import com.trading.journal.authentication.email.service.EmailSender;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationFields;
import com.trading.journal.authentication.verification.VerificationStatus;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class PasswordServiceImplTest {

    @Mock
    ApplicationUserService applicationUserService;

    @Mock
    VerificationService verificationService;

    @Mock
    EmailSender emailSender;

    @InjectMocks
    PasswordServiceImpl passwordService;

    @DisplayName("Request for a password change")
    @Test
    void passwordChangeRequest() {
        String email = "mail@mail.com";
        ApplicationUser applicationUser = new ApplicationUser(1L, "UserAdm", "123456", "User", "Admin", email, true, true, Collections.singletonList(new UserAuthority(1L, 1L, 1L, "ROLE_USER")), LocalDateTime.now());

        when(applicationUserService.getUserByEmail(email)).thenReturn(Mono.just(applicationUser));
        when(verificationService.send(VerificationType.CHANGE_PASSWORD, applicationUser)).thenReturn(Mono.empty());

        Mono<Void> voidMono = passwordService.requestPasswordChange(email);
        StepVerifier.create(voidMono).expectNextCount(0).verifyComplete();
    }

    @DisplayName("Request for a password change when user does not exist return exception")
    @Test
    void passwordChangeRequestException() {
        String email = "mail@mail.com";
        when(applicationUserService.getUserByEmail(email)).thenReturn(Mono.empty());

        Mono<Void> voidMono = passwordService.requestPasswordChange(email);
        StepVerifier.create(voidMono).expectErrorMatches(throwable -> throwable instanceof UsernameNotFoundException && throwable.getMessage().equals(String.format("User %s does not exist", email))).verify();

        verify(verificationService, never()).send(any(), any());
    }

    @DisplayName("Password change hash not found return exception")
    @Test
    void passwordChangeHashNotFound() {
        ChangePassword changePassword = new ChangePassword("mail@email.com", "null", "dad231#$#4", "dad231#$#4123");

        when(verificationService.retrieve(changePassword.hash())).thenReturn(Mono.empty());

        Mono<Void> voidMono = passwordService.changePassword(changePassword);
        StepVerifier.create(voidMono).expectErrorMatches(throwable -> throwable instanceof ApplicationException && ((ApplicationException) throwable).getStatusCode().equals(HttpStatus.BAD_REQUEST) && ((ApplicationException) throwable).getStatusText().equals("Change password request is invalid")).verify();

        verify(applicationUserService, never()).getUserByEmail(anyString());
        verify(emailSender, never()).send(any());
        verify(verificationService, never()).verify(any());
    }

    @DisplayName("Password change hash email and change request email are different return exception")
    @Test
    void passwordChangeHashDifferentEmail() {
        String email = "mail@email.com";
        String hash = UUID.randomUUID().toString();

        ChangePassword changePassword = new ChangePassword(email, hash, "dad231#$#4", "dad231#$#4123");
        Verification verification = new Verification(1L, "anotheremail@mail.com", VerificationType.CHANGE_PASSWORD, VerificationStatus.PENDING, hash, LocalDateTime.now());

        when(verificationService.retrieve(changePassword.hash())).thenReturn(Mono.just(verification));

        Mono<Void> voidMono = passwordService.changePassword(changePassword);
        StepVerifier.create(voidMono).expectErrorMatches(throwable -> throwable instanceof ApplicationException && ((ApplicationException) throwable).getStatusCode().equals(HttpStatus.BAD_REQUEST) && ((ApplicationException) throwable).getStatusText().equals("Change password request is invalid")).verify();

        verify(applicationUserService, never()).getUserByEmail(anyString());
        verify(emailSender, never()).send(any());
        verify(verificationService, never()).verify(any());
    }

    @DisplayName("Password change verification is not CHANGE_PASSWORD return exception")
    @Test
    void passwordChangeNotCHANGE_PASSWORD() {
        String email = "mail@email.com";
        String hash = UUID.randomUUID().toString();

        ChangePassword changePassword = new ChangePassword(email, hash, "dad231#$#4", "dad231#$#4123");
        Verification verification = new Verification(1L, email, VerificationType.REGISTRATION, VerificationStatus.PENDING, hash, LocalDateTime.now());

        when(verificationService.retrieve(changePassword.hash())).thenReturn(Mono.just(verification));

        Mono<Void> voidMono = passwordService.changePassword(changePassword);
        StepVerifier.create(voidMono).expectErrorMatches(throwable -> throwable instanceof ApplicationException && ((ApplicationException) throwable).getStatusCode().equals(HttpStatus.BAD_REQUEST) && ((ApplicationException) throwable).getStatusText().equals("Change password request is invalid")).verify();

        verify(applicationUserService, never()).getUserByEmail(anyString());
        verify(emailSender, never()).send(any());
        verify(verificationService, never()).verify(any());
    }

    @DisplayName("Password change verification executed successfully")
    @Test
    void passwordChange() {
        String email = "mail@email.com";
        String hash = UUID.randomUUID().toString();
        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserName",
                "12345679",
                "firstName",
                "lastName",
                email,
                true,
                true,
                Collections.emptyList(),
                LocalDateTime.now());

        EmailRequest emailRequest = new EmailRequest(
                "Confirmação de alteração senha",
                "mail/change-password-confirmation.html",
                singletonList(new EmailField("$NAME", "firstName lastName")),
                singletonList(applicationUser.getEmail()));

        ChangePassword changePassword = new ChangePassword(email, hash, "dad231#$#4", "dad231#$#4123");
        Verification verification = new Verification(1L, email, VerificationType.CHANGE_PASSWORD, VerificationStatus.PENDING, hash, LocalDateTime.now());

        when(verificationService.retrieve(changePassword.hash())).thenReturn(Mono.just(verification));
        when(applicationUserService.changePassword(email, "dad231#$#4")).thenReturn(Mono.empty());
        when(applicationUserService.getUserByEmail(changePassword.email())).thenReturn(Mono.just(applicationUser));
        when(emailSender.send(emailRequest)).thenReturn(Mono.empty());
        when(verificationService.verify(verification)).thenReturn(Mono.empty());

        Mono<Void> voidMono = passwordService.changePassword(changePassword);
        StepVerifier.create(voidMono).expectNextCount(0).verifyComplete();
    }
}