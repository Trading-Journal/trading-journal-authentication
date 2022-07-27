package com.trading.journal.authentication.verification.service.impl;

import com.trading.journal.authentication.configuration.properties.HostProperties;
import com.trading.journal.authentication.email.EmailField;
import com.trading.journal.authentication.email.EmailRequest;
import com.trading.journal.authentication.email.service.EmailSender;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationFields;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

@Service
@RequiredArgsConstructor
public class VerificationEmailServiceImpl implements VerificationEmailService {

    private final EmailSender emailSender;

    private final HostProperties hostProperties;

    @Override
    public void sendEmail(Verification verification, User applicationUser) {
        EmailRequest emailRequest = buildEmailRequest(verification, applicationUser);
        emailSender.send(emailRequest);
    }

    private EmailRequest buildEmailRequest(Verification verification, User applicationUser) {
        String name = applicationUser.getFirstName().concat(" ").concat(applicationUser.getLastName());
        return switch (verification.getType()) {
            case REGISTRATION -> new EmailRequest(
                    "Confirme seu endereço de e-mail",
                    VerificationFields.REGISTRATION_EMAIL_TEMPLATE.getValue(),
                    getEmailFields(name, hostProperties.getVerificationPage(), verification),
                    singletonList(applicationUser.getEmail()));
            case CHANGE_PASSWORD -> new EmailRequest(
                    "Confirmação para alterar sua senha",
                    VerificationFields.CHANGE_PASSWORD_EMAIL_TEMPLATE.getValue(),
                    getEmailFields(name, hostProperties.getChangePasswordPage(), verification),
                    singletonList(applicationUser.getEmail()));
            case ADMIN_REGISTRATION -> new EmailRequest(
                    "Voçê foi incluido como administrador do sistema",
                    VerificationFields.ADMIN_REGISTRATION_EMAIL_TEMPLATE.getValue(),
                    getEmailFields(name, hostProperties.getVerificationPage(), verification),
                    singletonList(applicationUser.getEmail()));
            case NEW_ORGANISATION_USER -> new EmailRequest(
                    "Voçê foi incluido como usuário do sistema",
                    VerificationFields.NEW_ORGANISATION_USER.getValue(),
                    getEmailFields(name, hostProperties.getVerificationPage(), verification),
                    singletonList(applicationUser.getEmail()));
        };
    }

    private List<EmailField> getEmailFields(String name, String webpage, Verification verification) {
        return Arrays.asList(
                new EmailField(VerificationFields.USER_NAME.getValue(), name),
                new EmailField(VerificationFields.URL.getValue(), UriComponentsBuilder.newInstance()
                        .uri(URI.create(hostProperties.getFrontEnd()))
                        .path(webpage)
                        .queryParam(VerificationFields.HASH.getValue(), verification.getHash())
                        .build()
                        .toUriString())
        );
    }
}
