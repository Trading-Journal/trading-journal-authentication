package com.trading.journal.authentication.verification.service.impl;

import com.trading.journal.authentication.configuration.properties.HostProperties;
import com.trading.journal.authentication.email.EmailField;
import com.trading.journal.authentication.email.EmailRequest;
import com.trading.journal.authentication.email.service.EmailSender;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
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

    private final HostProperties hostProperties;

    @Override
    public Mono<Void> sendEmail(Verification verification, ApplicationUser applicationUser) {
        EmailRequest emailRequest = buildEmailRequest(verification, applicationUser);
        return emailSender.send(emailRequest);
    }

    private EmailRequest buildEmailRequest(Verification verification, ApplicationUser applicationUser) {
        String name = applicationUser.getFirstName().concat(" ").concat(applicationUser.getLastName());
        String url = UriComponentsBuilder.newInstance()
                .uri(URI.create(hostProperties.getFrontEnd()))
                .path(hostProperties.getVerificationPage())
                .queryParam(VerificationFields.HASH.getValue(), verification.getHash())
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
