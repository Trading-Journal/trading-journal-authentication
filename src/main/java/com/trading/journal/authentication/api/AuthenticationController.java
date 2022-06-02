package com.trading.journal.authentication.api;

import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.authentication.service.AuthenticationService;
import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class AuthenticationController implements AuthenticationApi {

    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;

    @Override
    public Mono<ResponseEntity<SignUpResponse>> signUp(@Valid UserRegistration registration) {
        return registrationService.signUp(registration).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<LoginResponse>> signIn(@Valid Login login) {
        return authenticationService.signIn(login).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<LoginResponse>> refreshToken(String refreshToken) {
        return authenticationService.refreshToken(refreshToken).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> verify(String hash) {
        return registrationService.verify(hash).map(ResponseEntity::ok);
    }

}
