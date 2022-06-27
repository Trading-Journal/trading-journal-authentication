package com.trading.journal.authentication.registration.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.userauthority.UserAuthority;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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
                Collections.singletonList(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))),
                LocalDateTime.now());

        when(applicationUserService.createNewUser(userRegistration)).thenReturn(applicationUser);
        when(verificationProperties.isEnabled()).thenReturn(false);

        SignUpResponse signUpResponse = registrationService.signUp(userRegistration);

        assertThat(signUpResponse.email()).isEqualTo("mail@mail.com");
        assertThat(signUpResponse.enabled()).isTrue();
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
                Collections.singletonList(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))),
                LocalDateTime.now());

        when(applicationUserService.createNewUser(userRegistration)).thenReturn(applicationUser);
        when(applicationUserService.getUserByEmail("mail@mail.com")).thenReturn(applicationUser);
        when(verificationProperties.isEnabled()).thenReturn(true);
        doNothing().when(verificationService).send(VerificationType.REGISTRATION, applicationUser);

        SignUpResponse signUpResponse = registrationService.signUp(userRegistration);

        assertThat(signUpResponse.email()).isEqualTo("mail@mail.com");
        assertThat(signUpResponse.enabled()).isFalse();
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

        when(verificationService.retrieve(hash)).thenReturn(verification);
        doNothing().when(applicationUserService).verifyNewUser(verification.getEmail());
        doNothing().when(verificationService).verify(verification);

        registrationService.verify(hash);
    }

    @Test
    @DisplayName("Process email verification return exception when retrieving the hash values do not execute all process")
    void emailVerificationError() {
        String hash = UUID.randomUUID().toString();
        when(verificationService.retrieve(hash)).thenThrow(new ApplicationException("any error message"));

        assertThrows(ApplicationException.class, () -> registrationService.verify(hash), "any error message");
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
                Collections.singletonList(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))),
                LocalDateTime.now());

        when(verificationProperties.isEnabled()).thenReturn(true);
        when(applicationUserService.getUserByEmail(email)).thenReturn(applicationUser);
        doNothing().when(verificationService).send(VerificationType.REGISTRATION, applicationUser);

        SignUpResponse signUpResponse = registrationService.sendVerification(email);
        assertThat(signUpResponse.email()).isEqualTo(email);
        assertThat(signUpResponse.enabled()).isFalse();
    }

    @Test
    @DisplayName("Send new email verification when verification process is disabled, do dot send verification email")
    void newEmailVerificationDisabled() {
        String email = "mail@mail.com";
        when(verificationProperties.isEnabled()).thenReturn(false);

        SignUpResponse signUpResponse = registrationService.sendVerification(email);
        assertThat(signUpResponse.email()).isEqualTo(email);
        assertThat(signUpResponse.enabled()).isTrue();

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
                Collections.singletonList(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))),
                LocalDateTime.now());

        when(verificationProperties.isEnabled()).thenReturn(true);
        when(applicationUserService.getUserByEmail(email)).thenReturn(applicationUser);

        SignUpResponse signUpResponse = registrationService.sendVerification(email);
        assertThat(signUpResponse.email()).isEqualTo(email);
        assertThat(signUpResponse.enabled()).isTrue();

        verify(verificationService, never()).send(any(), any());
    }
}
