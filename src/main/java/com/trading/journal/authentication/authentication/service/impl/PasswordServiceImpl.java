package com.trading.journal.authentication.authentication.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authentication.ChangePassword;
import com.trading.journal.authentication.authentication.service.PasswordService;
import com.trading.journal.authentication.email.EmailField;
import com.trading.journal.authentication.email.EmailRequest;
import com.trading.journal.authentication.email.service.EmailSender;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static java.util.Collections.singletonList;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private static final String CONFIRMATION_PASSWORD_EMAIL_TEMPLATE = "mail/change-password-confirmation.html";
    private static final String NAME = "$NAME";
    private final ApplicationUserService applicationUserService;

    private final VerificationService verificationService;

    private final EmailSender emailSender;

    @Override
    public Mono<Void> requestPasswordChange(String email) {
        return applicationUserService.getUserByEmail(email)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException(String.format("User %s does not exist", email))))
                .flatMap(applicationUser -> verificationService.send(VerificationType.CHANGE_PASSWORD, applicationUser))
                .name("password_change_request")
                .metrics();
    }

    @Override
    public Mono<Void> changePassword(ChangePassword changePassword) {
        return verificationService.retrieve(changePassword.hash())
                .filter(verification -> validateVerification(changePassword, verification))
                .switchIfEmpty(Mono.error(new ApplicationException(HttpStatus.BAD_REQUEST, "Change password request is invalid")))
                .flatMap(verification -> applicationUserService.changePassword(changePassword.email(), changePassword.getPassword())
                        .flatMap(applicationUser -> emailSender.send(emailRequest(applicationUser)))
                        .then(verificationService.verify(verification))
                )
                .name("password_change")
                .metrics();
    }

    private EmailRequest emailRequest(ApplicationUser applicationUser) {
        return new EmailRequest(
                "Confirmação de alteração senha",
                CONFIRMATION_PASSWORD_EMAIL_TEMPLATE,
                singletonList(new EmailField(NAME, applicationUser.getFirstName().concat(" ").concat(applicationUser.getLastName()))),
                singletonList(applicationUser.getEmail())
        );
    }

    private boolean validateVerification(ChangePassword changePassword, Verification verification) {
        return verification.getEmail().equals(changePassword.email()) && VerificationType.CHANGE_PASSWORD.equals(verification.getType());
    }
}
