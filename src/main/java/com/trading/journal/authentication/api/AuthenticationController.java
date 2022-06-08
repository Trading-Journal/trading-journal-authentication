package com.trading.journal.authentication.api;

import com.trading.journal.authentication.password.ChangePassword;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.authentication.service.AuthenticationService;
import com.trading.journal.authentication.password.service.PasswordManagementService;
import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class AuthenticationController implements AuthenticationApi {

    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;

    private final PasswordManagementService passwordManagementService;

    @Override
    public ResponseEntity<SignUpResponse> signUp(@Valid UserRegistration registration) {
        SignUpResponse signUpResponse = registrationService.signUp(registration);
        return ok(signUpResponse);
    }

    @Override
    public ResponseEntity<LoginResponse> signIn(@Valid Login login) {
        LoginResponse loginResponse = authenticationService.signIn(login);
        return ok(loginResponse);
    }

    @Override
    public ResponseEntity<LoginResponse> refreshToken(String refreshToken) {
        LoginResponse loginResponse = authenticationService.refreshToken(refreshToken);
        return ok(loginResponse);
    }

    @Override
    public ResponseEntity<Void> verify(String hash) {
        registrationService.verify(hash);
        return ok().build();
    }

    @Override
    public ResponseEntity<SignUpResponse> sendVerification(String email) {
        SignUpResponse signUpResponse = registrationService.sendVerification(email);
        return ok(signUpResponse);
    }

    @Override
    public ResponseEntity<Void> requestPasswordChange(String email) {
        passwordManagementService.requestPasswordChange(email);
        return ok().build();
    }

    @Override
    public ResponseEntity<Void> changePassword(@Valid ChangePassword changePassword) {
        passwordManagementService.changePassword(changePassword);
        return ok().build();
    }
}
