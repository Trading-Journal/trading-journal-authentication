package com.trading.journal.authentication.api;

import com.trading.journal.authentication.password.ChangePassword;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Authentication Api")
@RequestMapping("/auth")
public interface AuthenticationApi {

    @ApiOperation(notes = "Sign up as a new user", value = "Sign up", response = SignUpResponse.class)
    @ApiResponses(@ApiResponse(code = 200, message = "New user created"))
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<SignUpResponse> signUp(@RequestBody UserRegistration registration);

    @ApiOperation(notes = "Sign in", value = "Sign in", response = LoginResponse.class)
    @ApiResponses(@ApiResponse(code = 200, message = "User logged in"))
    @PostMapping("/signin")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<LoginResponse> signIn(@RequestBody Login login);

    @ApiOperation(notes = "Refresh token", value = "Refresh access token", response = LoginResponse.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Access token refreshed"))
    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<LoginResponse> refreshToken(@RequestHeader("refresh-token") String refreshToken);

    @ApiOperation(notes = "Verification for new user", value = "Email verification")
    @ApiResponses({
            @ApiResponse(code = 200, message = "User verified"),
            @ApiResponse(code = 400, message = "Verification does not exist or is invalid")})
    @PostMapping("/verify")
    ResponseEntity<Void> verify(@RequestParam("hash") String hash);

    @ApiOperation(notes = "Send verification link for new user again", value = "Send verification link", response = SignUpResponse.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Verification sent"))
    @PostMapping("/verify/send")
    ResponseEntity<SignUpResponse> sendVerification(@RequestParam("email") String email);

    @ApiOperation(notes = "Request a url to change password", value = "Request to change password")
    @ApiResponses(@ApiResponse(code = 200, message = "Change password url sent"))
    @PostMapping("/change-password/request")
    ResponseEntity<Void> requestPasswordChange(@RequestParam("email") String email);

    @ApiOperation(notes = "Change user password", value = "Change user password")
    @ApiResponses(@ApiResponse(code = 200, message = "User logged password changes"))
    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> changePassword(@RequestBody ChangePassword changePassword);
}