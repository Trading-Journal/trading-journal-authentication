package com.trading.journal.authentication.api;

import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.password.ChangePassword;
import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
public interface AuthenticationApi {

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<SignUpResponse> signUp(@RequestBody UserRegistration registration);

    @PostMapping("/signin")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<LoginResponse> signIn(@RequestBody Login login);

    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<LoginResponse> refreshToken(@RequestHeader("refresh-token") String refreshToken);

    @PostMapping("/verify")
    ResponseEntity<Void> verify(@RequestParam("hash") String hash);

    @PostMapping("/verify/send")
    ResponseEntity<SignUpResponse> sendVerification(@RequestParam("email") String email);

    @PostMapping("/change-password/request")
    ResponseEntity<Void> requestPasswordChange(@RequestParam("email") String email);

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> changePassword(@RequestBody ChangePassword changePassword);

    @GetMapping("/hello")
    ResponseEntity<String> hello();
}