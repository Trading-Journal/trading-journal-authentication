package com.trading.journal.authentication.verification.impl;

import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.email.EmailField;
import com.trading.journal.authentication.email.EmailRequest;
import com.trading.journal.authentication.email.EmailSender;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationStatus;
import com.trading.journal.authentication.verification.VerificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class VerificationEmailServiceImplTest {

    @Mock
    EmailSender emailSender;

    @InjectMocks
    VerificationEmailServiceImpl verificationEmailService;

    @DisplayName("Given verification and application user end and email with correct URL")
    @Test
    void sendEmail() {
        String hash = UUID.randomUUID().toString();

        Verification verification = new Verification(1L, "mail@mail.com", VerificationType.REGISTRATION, VerificationStatus.PENDING, hash, LocalDateTime.now());

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

        String url = String.format("http://localhost:8080/authentication/verify?hash=%s&email=%s", hash, "mail@mail.com");
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

        when(emailSender.send(emailRequest)).thenReturn(Mono.empty());

        Mono<Void> voidMono = verificationEmailService.sendEmail(verification, applicationUser);

        StepVerifier.create(voidMono)
                .verifyComplete();
    }
}