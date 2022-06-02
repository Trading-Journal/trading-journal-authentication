package com.trading.journal.authentication.registration.service.impl;

import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.registration.service.RegistrationService;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.properties.VerificationProperties;
import com.trading.journal.authentication.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final ApplicationUserService applicationUserService;

    private final VerificationService verificationService;

    private final VerificationProperties verificationProperties;

    @Override
    public Mono<SignUpResponse> signUp(@Valid UserRegistration userRegistration) {
        return applicationUserService.createNewUser(userRegistration)
                .flatMap(this::sendVerification)
                .name("signup_user")
                .metrics();
    }

    @Override
    public Mono<Void> verify(String hash) {
        return verificationService.retrieve(hash)
                .flatMap(verification -> applicationUserService.verifyNewUser(verification.getEmail())
                        .then(verificationService.verify(verification))
                )
                .name("verify_new_user")
                .metrics();
    }

    @Override
    public Mono<SignUpResponse> sendVerification(String email) {
        SignUpResponse signUpResponse = new SignUpResponse(email, true);
        Mono<SignUpResponse> methodReturn = Mono.just(signUpResponse);
        if (verificationProperties.isEnabled()) {
            methodReturn = applicationUserService.getUserByEmail(email)
                    .flatMap(applicationUser -> {
                        if (applicationUser.getEnabled()) {
                            return Mono.just(signUpResponse);
                        } else {
                            return this.sendVerification(applicationUser);
                        }
                    });
        }
        return methodReturn;
    }

    private Mono<SignUpResponse> sendVerification(ApplicationUser applicationUser) {
        SignUpResponse signUpResponse = new SignUpResponse(applicationUser.getEmail(), applicationUser.getEnabled());
        Mono<SignUpResponse> signUpResponseMono;
        if (verificationProperties.isDisabled() || applicationUser.getEnabled().equals(true)) {
            signUpResponseMono = Mono.just(signUpResponse);
        } else {
            signUpResponseMono = verificationService.send(VerificationType.REGISTRATION, applicationUser)
                    .thenReturn(signUpResponse);
        }
        return signUpResponseMono;
    }

}
