package com.trading.journal.authentication.api;

import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import reactor.core.publisher.Mono;

@Api(tags = "Authentication Api")
@RequestMapping("/authentication")
public interface AuthenticationApi {

    @ApiOperation(notes = "Sign up as a new user", value = "Sign up", response = SignUpResponse.class)
    @ApiResponses(@ApiResponse(code = 200, message = "New user created"))
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.OK)
    Mono<ResponseEntity<SignUpResponse>> signUp(@RequestBody UserRegistration registration);

    @ApiOperation(notes = "Sign in", value = "Sign in", response = LoginResponse.class)
    @ApiResponses(@ApiResponse(code = 200, message = "User logged in"))
    @PostMapping("/signin")
    @ResponseStatus(HttpStatus.OK)
    Mono<ResponseEntity<LoginResponse>> signIn(@RequestBody Login login);

    @ApiOperation(notes = "Refresh token", value = "Refresh access token", response = LoginResponse.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Access token refreshed"))
    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    Mono<ResponseEntity<LoginResponse>> refreshToken(@RequestHeader("refresh-token") String refreshToken);
}
