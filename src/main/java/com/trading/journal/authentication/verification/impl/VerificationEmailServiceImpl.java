package com.trading.journal.authentication.verification.impl;

import com.trading.journal.authentication.email.EmailField;
import com.trading.journal.authentication.email.EmailRequest;
import com.trading.journal.authentication.email.EmailSender;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationEmailService;
import com.trading.journal.authentication.verification.VerificationFields;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;

import static java.util.Collections.singletonList;

@Service
@RequiredArgsConstructor
public class VerificationEmailServiceImpl implements VerificationEmailService {

    private final EmailSender emailSender;

    @Override
    public Mono<Void> sendEmail(Verification verification, ApplicationUser applicationUser) {
        EmailRequest emailRequest = buildEmailRequest(verification, applicationUser);
        return emailSender.send(emailRequest);
    }

    private EmailRequest buildEmailRequest(Verification verification, ApplicationUser applicationUser) {
        String name = applicationUser.getFirstName().concat(" ").concat(applicationUser.getLastName());
        String url = UriComponentsBuilder.newInstance()
                .uri(URI.create("http://localhost:8080"))
                .path(VerificationFields.PATH.getValue())
                .queryParam(VerificationFields.HASH.getValue(), verification.getHash())
                .queryParam(VerificationFields.EMAIL.getValue(), verification.getEmail())
                .build()
                .toUriString();

        return new EmailRequest(
                "Confirme seu endereço de e-mail",
                VerificationFields.EMAIL_TEMPLATE.getValue(),
                Arrays.asList(
                        new EmailField(VerificationFields.USER_NAME.getValue(), name),
                        new EmailField(VerificationFields.URL.getValue(), url)
                ),
                singletonList(applicationUser.getEmail())
        );
    }
}
