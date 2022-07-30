package com.trading.journal.authentication.verification.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.service.UserService;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.properties.VerificationProperties;
import com.trading.journal.authentication.verification.service.HashProvider;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import com.trading.journal.authentication.verification.VerificationRepository;
import com.trading.journal.authentication.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationRepository verificationRepository;
    private final VerificationEmailService verificationEmailService;

    private final UserService userService;

    private final HashProvider hashProvider;

    private final VerificationProperties verificationProperties;

    @Override
    public void send(VerificationType verificationType, User applicationUser) {
        Verification verification = verificationRepository.getByTypeAndEmail(verificationType, applicationUser.getEmail())
                .orElseGet(() -> Verification.builder().email(applicationUser.getEmail()).type(verificationType).build());
        if(doNotSendVerification(verification)) {
            return;
        }
        verification = verification.renew(hashProvider.generateHash(verification.getEmail()));
        verification = verificationRepository.save(verification);
        verificationEmailService.sendEmail(verification, applicationUser);
    }

    @Override
    public Verification retrieve(String hash) {
        String email = hashProvider.readHashValue(hash);
        return verificationRepository.getByHashAndEmail(hash, email)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "Request is invalid"));
    }

    @Override
    public void verify(Verification verification) {
        if (shouldSendChangePassword(verification)) {
            sendChangePassword(verification);
        }
        verificationRepository.delete(verification);
    }

    private void sendChangePassword(Verification verification) {
        User applicationUser = userService.getUserByEmail(verification.getEmail());
        this.send(VerificationType.CHANGE_PASSWORD, applicationUser);
    }

    private static boolean shouldSendChangePassword(Verification verification) {
        return VerificationType.ADMIN_REGISTRATION.equals(verification.getType())
                || VerificationType.NEW_ORGANISATION_USER.equals(verification.getType());
    }

    private boolean doNotSendVerification(Verification verification) {
        return isUserRegistration(verification) && !verificationProperties.isEnabled();
    }

    private static boolean isUserRegistration(Verification verification) {
        return VerificationType.ADMIN_REGISTRATION.equals(verification.getType())
                || VerificationType.REGISTRATION.equals(verification.getType())
                || VerificationType.NEW_ORGANISATION_USER.equals(verification.getType());
    }
}
