package com.trading.journal.authentication.registration.service.impl;

import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.registration.service.RegistrationService;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.verification.VerificationType;
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

    private Mono<SignUpResponse> sendVerification(ApplicationUser applicationUser) {
        SignUpResponse signUpResponse = new SignUpResponse(applicationUser.getEmail(), applicationUser.getEnabled());
        Mono<SignUpResponse> signUpResponseMono;
        if (applicationUser.getEnabled().equals(false)) {
            signUpResponseMono = verificationService.send(VerificationType.REGISTRATION, applicationUser)
                    .thenReturn(signUpResponse);
        } else {
            signUpResponseMono = Mono.just(signUpResponse);
        }
        return signUpResponseMono;
    }

}
