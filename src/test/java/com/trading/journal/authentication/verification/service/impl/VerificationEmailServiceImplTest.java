package com.trading.journal.authentication.verification.service.impl;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.configuration.properties.HostProperties;
import com.trading.journal.authentication.email.EmailField;
import com.trading.journal.authentication.email.EmailRequest;
import com.trading.journal.authentication.email.service.EmailSender;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationStatus;
import com.trading.journal.authentication.verification.VerificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class VerificationEmailServiceImplTest {

    @Mock
    EmailSender emailSender;

    @Mock
    HostProperties hostProperties;

    @InjectMocks
    VerificationEmailServiceImpl verificationEmailService;

    @DisplayName("Given verification REGISTRATION and application user end and email with correct URL")
    @Test
    void sendRegistration() {
        String hash = UUID.randomUUID().toString();

        when(hostProperties.getFrontEnd()).thenReturn("http://site.com");
        when(hostProperties.getVerificationPage()).thenReturn("auth/email-verified");
        Verification verification = new Verification(1L, "mail@mail.com", VerificationType.REGISTRATION, VerificationStatus.PENDING, hash, LocalDateTime.now());

        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))))
                .build();

        String url = String.format("http://site.com/auth/email-verified?hash=%s", hash);
        List<EmailField> fields = Arrays.asList(
                new EmailField("$NAME", "User Admin"),
                new EmailField("$URL", url)
        );

        EmailRequest emailRequest = new EmailRequest(
                "Confirme seu endereço de e-mail",
                "mail/verification.html",
                fields,
                singletonList("mail@mail.com")
        );

        doNothing().when(emailSender).send(emailRequest);

        verificationEmailService.sendEmail(verification, applicationUser);
    }

    @DisplayName("Given verification CHANGE_PASSWORD and application user end and email with correct URL")
    @Test
    void sendChangePassword() {
        String hash = UUID.randomUUID().toString();

        when(hostProperties.getFrontEnd()).thenReturn("http://site.com");
        when(hostProperties.getChangePasswordPage()).thenReturn("auth/change-password");
        Verification verification = new Verification(1L, "mail@mail.com", VerificationType.CHANGE_PASSWORD, VerificationStatus.PENDING, hash, LocalDateTime.now());

        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))))
                .build();

        String url = String.format("http://site.com/auth/change-password?hash=%s", hash);
        List<EmailField> fields = Arrays.asList(
                new EmailField("$NAME", "User Admin"),
                new EmailField("$URL", url)
        );

        EmailRequest emailRequest = new EmailRequest(
                "Confirmação para alterar sua senha",
                "mail/change-password.html",
                fields,
                singletonList("mail@mail.com")
        );

        doNothing().when(emailSender).send(emailRequest);

        verificationEmailService.sendEmail(verification, applicationUser);
    }

    @DisplayName("Given verification ADMIN_REGISTRATION and application user end and email with correct URL")
    @Test
    void sendAdminRegistration() {
        String hash = UUID.randomUUID().toString();

        when(hostProperties.getFrontEnd()).thenReturn("http://site.com");
        when(hostProperties.getVerificationPage()).thenReturn("auth/email-verified");
        Verification verification = new Verification(1L, "mail@mail.com", VerificationType.ADMIN_REGISTRATION, VerificationStatus.PENDING, hash, LocalDateTime.now());

        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))))
                .build();

        String url = String.format("http://site.com/auth/email-verified?hash=%s", hash);
        List<EmailField> fields = Arrays.asList(
                new EmailField("$NAME", "User Admin"),
                new EmailField("$URL", url)
        );

        EmailRequest emailRequest = new EmailRequest(
                "Voçê foi incluido como administrador do sistema",
                "mail/admin-registration.html",
                fields,
                singletonList("mail@mail.com")
        );

        doNothing().when(emailSender).send(emailRequest);

        verificationEmailService.sendEmail(verification, applicationUser);
    }

    @DisplayName("Given verification NEW_ORGANISATION_USER and application user end and email with correct URL")
    @Test
    void sendOrgRegistration() {
        String hash = UUID.randomUUID().toString();

        when(hostProperties.getFrontEnd()).thenReturn("http://site.com");
        when(hostProperties.getVerificationPage()).thenReturn("auth/email-verified");
        Verification verification = new Verification(1L, "mail@mail.com", VerificationType.NEW_ORGANISATION_USER, VerificationStatus.PENDING, hash, LocalDateTime.now());

        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(List.of(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))))
                .build();

        String url = String.format("http://site.com/auth/email-verified?hash=%s", hash);
        List<EmailField> fields = Arrays.asList(
                new EmailField("$NAME", "User Admin"),
                new EmailField("$URL", url)
        );

        EmailRequest emailRequest = new EmailRequest(
                "Voçê foi incluido como usuário do sistema",
                "mail/organisation-verification.html",
                fields,
                singletonList("mail@mail.com")
        );

        doNothing().when(emailSender).send(emailRequest);

        verificationEmailService.sendEmail(verification, applicationUser);
    }
}