package com.trading.journal.authentication.verification.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.HashProvider;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import com.trading.journal.authentication.verification.service.VerificationRepository;
import com.trading.journal.authentication.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationRepository verificationRepository;
    private final VerificationEmailService verificationEmailService;

    private final ApplicationUserService applicationUserService;

    private final HashProvider hashProvider;

    @Override
    public void send(VerificationType verificationType, ApplicationUser applicationUser) {
        Verification verification = verificationRepository.getByTypeAndEmail(verificationType, applicationUser.getEmail());
        if (verification == null) {
            verification = Verification.builder().email(applicationUser.getEmail()).type(verificationType).build();
        } else {
            verification = verification.renew(hashProvider.generateHash(verification.getEmail()));
        }
        verification = verificationRepository.save(verification);
        verificationEmailService.sendEmail(verification, applicationUser);
    }

    @Override
    public Verification retrieve(String hash) {
        String email = hashProvider.readHashValue(hash);
        Verification verification = verificationRepository.getByHashAndEmail(hash, email);
        if(verification == null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Request is invalid");
        }
        return verification;
    }

    @Override
    public void verify(Verification verification) {
        if (VerificationType.ADMIN_REGISTRATION.equals(verification.getType())) {
            ApplicationUser applicationUser = applicationUserService.getUserByEmail(verification.getEmail());
            this.send(VerificationType.CHANGE_PASSWORD, applicationUser);
        }
        verificationRepository.delete(verification);
    }
}
