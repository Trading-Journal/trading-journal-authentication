package com.trading.journal.authentication.registration.service.impl;

import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.registration.service.RegistrationService;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.properties.VerificationProperties;
import com.trading.journal.authentication.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final ApplicationUserService applicationUserService;

    private final VerificationService verificationService;

    private final VerificationProperties verificationProperties;

    @Override
    public SignUpResponse signUp(@Valid UserRegistration userRegistration) {
        ApplicationUser applicationUser = applicationUserService.createNewUser(userRegistration);
        return sendVerification(applicationUser.getEmail());
    }

    @Override
    public void verify(String hash) {
        Verification verification = verificationService.retrieve(hash);
        applicationUserService.verifyNewUser(verification.getEmail());
        verificationService.verify(verification);
    }

    @Override
    public SignUpResponse sendVerification(String email) {
        SignUpResponse signUpResponse = new SignUpResponse(email, true);
        if (verificationProperties.isEnabled()) {
            ApplicationUser applicationUser = applicationUserService.getUserByEmail(email);
            if (!applicationUser.getEnabled()) {
                verificationService.send(VerificationType.REGISTRATION, applicationUser);
                signUpResponse = new SignUpResponse(email, false);
            }
        }
        return signUpResponse;
    }
}
