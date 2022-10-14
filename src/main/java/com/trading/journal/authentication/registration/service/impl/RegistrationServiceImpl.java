package com.trading.journal.authentication.registration.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.registration.service.RegistrationService;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.TenancyException;
import com.trading.journal.authentication.tenancy.service.TenancyService;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.service.UserService;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.properties.VerificationProperties;
import com.trading.journal.authentication.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserService userService;

    private final TenancyService tenancyService;

    private final UserAuthorityService userAuthorityService;

    private final VerificationService verificationService;

    private final VerificationProperties verificationProperties;

    @Override
    public SignUpResponse signUp(@Valid UserRegistration userRegistration) {
        Tenancy tenancy;
        try {
            tenancy = tenancyService.create(Tenancy.builder().name(userRegistration.getCompanyName()).userUsage(1).build());
        } catch (TenancyException e) {
            throw (ApplicationException) new ApplicationException(e.getStatusCode(), "Organisation already exist").initCause(e);
        }

        User user;
        try {
            user = userService.createNewUser(userRegistration, tenancy);
        } catch (ApplicationException e) {
            tenancyService.delete(tenancy.getId());
            throw e;
        }

        userAuthorityService.saveOrganisationAdminUserAuthorities(user);
        return sendVerification(user.getEmail());
    }

    @Override
    public void verify(String hash) {
        Verification verification = verificationService.retrieve(hash);
        userService.verifyUser(verification.getEmail());
        verificationService.verify(verification);
    }

    @Override
    public SignUpResponse sendVerification(String email) {
        SignUpResponse signUpResponse = new SignUpResponse(email, true);
        if (verificationProperties.isEnabled()) {
            Optional<User> userByEmail = userService.getUserByEmail(email);
            if (userByEmail.isPresent() && !userByEmail.get().getEnabled()) {
                verificationService.send(VerificationType.REGISTRATION, userByEmail.get());
                signUpResponse = new SignUpResponse(email, false);
            }
        }
        return signUpResponse;
    }
}
