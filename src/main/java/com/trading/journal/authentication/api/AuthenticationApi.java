package com.trading.journal.authentication.api;

import com.trading.journal.authentication.registration.UserRegistration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

        @ApiOperation(notes = "Sign up as a new user", value = "Sign up")
        @ApiResponses({
                        @ApiResponse(code = 200, message = "New user created"),
                        @ApiResponse(code = 400, message = "Invalid data provided") })
        @PostMapping("/signup")
        @ResponseStatus(HttpStatus.OK)
        Mono<Void> signup(@RequestBody UserRegistration registration);

        // @ApiOperation(notes = "Sign in", value = "Sign in", response =
        // LoginResponse.class)
        // @ApiResponses({
        // @ApiResponse(code = 200, message = "User logged in"),
        // @ApiResponse(code = 400, message = Constants.HTTP_400_MESSAGE),
        // @ApiResponse(code = 401, message = Constants.HTTP_401_MESSAGE)})
        // @PostMapping("/signin")
        // @ResponseStatus(HttpStatus.OK)
        // Mono<LoginResponse> signin(@RequestBody Login login);

}
