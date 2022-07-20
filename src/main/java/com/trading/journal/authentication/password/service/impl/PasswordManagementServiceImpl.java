package com.trading.journal.authentication.password.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.email.EmailField;
import com.trading.journal.authentication.email.EmailRequest;
import com.trading.journal.authentication.email.service.EmailSender;
import com.trading.journal.authentication.password.ChangePassword;
import com.trading.journal.authentication.password.service.PasswordManagementService;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static java.util.Collections.singletonList;

@Service
@RequiredArgsConstructor
public class PasswordManagementServiceImpl implements PasswordManagementService {

    private static final String CONFIRMATION_PASSWORD_EMAIL_TEMPLATE = "mail/change-password-confirmation.html";
    private static final String NAME = "$NAME";
    private final ApplicationUserService applicationUserService;

    private final VerificationService verificationService;

    private final EmailSender emailSender;

    @Override
    public void requestPasswordChange(String email) {
        User applicationUser = applicationUserService.getUserByEmail(email);
        verificationService.send(VerificationType.CHANGE_PASSWORD, applicationUser);
        applicationUserService.unprovenUser(email);
    }

    @Override
    public void changePassword(ChangePassword changePassword) {
        Verification verification = verificationService.retrieve(changePassword.hash());
        if (validateVerification(changePassword, verification)) {
            User applicationUser = applicationUserService.changePassword(changePassword.email(), changePassword.getPassword());
            EmailRequest emailRequest = passwordChangeConfirmation(applicationUser);
            applicationUserService.verifyUser(applicationUser.getEmail());
            emailSender.send(emailRequest);
            verificationService.verify(verification);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Change password request is invalid");
        }
    }

    private EmailRequest passwordChangeConfirmation(User applicationUser) {
        return new EmailRequest(
                "Confirmação de alteração senha",
                CONFIRMATION_PASSWORD_EMAIL_TEMPLATE,
                singletonList(new EmailField(NAME, applicationUser.getFirstName().concat(" ").concat(applicationUser.getLastName()))),
                singletonList(applicationUser.getEmail())
        );
    }

    private boolean validateVerification(ChangePassword changePassword, Verification verification) {
        return verification != null && verification.getEmail().equals(changePassword.email()) && VerificationType.CHANGE_PASSWORD.equals(verification.getType());
    }
}
