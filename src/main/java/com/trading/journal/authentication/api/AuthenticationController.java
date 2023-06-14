package com.trading.journal.authentication.api;

import com.trading.journal.authentication.password.ChangePassword;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.authentication.service.AuthenticationService;
import com.trading.journal.authentication.password.service.PasswordManagementService;
import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.registration.service.RegistrationService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class AuthenticationController implements AuthenticationApi {

    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;

    private final PasswordManagementService passwordManagementService;

    @Timed(value = "signup_user", description = "Amount of time create a new user via signup")
    @Override
    public ResponseEntity<SignUpResponse> signUp(@Valid UserRegistration registration) {
        SignUpResponse signUpResponse = registrationService.signUp(registration);
        return ok(signUpResponse);
    }

    @Timed(value = "signin_user", description = "Amount of time to authenticate a user")
    @Override
    public ResponseEntity<LoginResponse> signIn(@Valid Login login) {
        LoginResponse loginResponse = authenticationService.signIn(login);
        return ok(loginResponse);
    }

    @Timed(value = "refresh_token", description = "Amount of time to refresh the user token")
    @Override
    public ResponseEntity<LoginResponse> refreshToken(String refreshToken) {
        LoginResponse loginResponse = authenticationService.refreshToken(refreshToken);
        return ok(loginResponse);
    }

    @Timed(value = "verify_new_user", description = "Amount of time to verify a new user email")
    @Override
    public ResponseEntity<Void> verify(String hash) {
        registrationService.verify(hash);
        return ok().build();
    }

    @Timed(value = "send_new_verification", description = "Amount of time to send a new email verification to the user email")
    @Override
    public ResponseEntity<SignUpResponse> sendVerification(String email) {
        SignUpResponse signUpResponse = registrationService.sendVerification(email);
        return ok(signUpResponse);
    }

    @Timed(value = "request_password_change", description = "Amount of time to request a password change")
    @Override
    public ResponseEntity<Void> requestPasswordChange(String email) {
        passwordManagementService.requestPasswordChange(email);
        return ok().build();
    }

    @Timed(value = "password_change", description = "Amount of time to apply a password change")
    @Override
    public ResponseEntity<Void> changePassword(@Valid ChangePassword changePassword) {
        passwordManagementService.changePassword(changePassword);
        return ok().build();
    }

    @Override
    public ResponseEntity<String> hello() {
        return ok("hello");
    }
}
