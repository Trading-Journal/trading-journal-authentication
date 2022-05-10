package com.trading.journal.authentication.api;

import javax.validation.Valid;

import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.registration.service.SignupService;

import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class AuthenticationController implements AuthenticationApi {

    private final SignupService signupService;

    public AuthenticationController(SignupService signupService) {
        this.signupService = signupService;
    }

    @Override
    public Mono<Void> signup(@Valid UserRegistration registration) {
        return signupService.signUp(registration);
    }

}
