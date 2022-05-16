package com.trading.journal.authentication.api;

import javax.validation.Valid;

import com.trading.journal.authentication.authentication.AuthenticationService;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.registration.service.RegistrationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class AuthenticationController implements AuthenticationApi {

    private final RegistrationService signupService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(RegistrationService signupService, AuthenticationService authenticationService) {
        this.signupService = signupService;
        this.authenticationService = authenticationService;
    }

    @Override
    public Mono<ResponseEntity<Void>> signup(@Valid UserRegistration registration) {
        return signupService.signUp(registration).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<LoginResponse>> signin(@Valid Login login) {
        return authenticationService.signIn(login).map(ResponseEntity::ok);
    }

}
