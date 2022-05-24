package com.trading.journal.authentication.authentication.impl;

import javax.validation.Valid;

import com.trading.journal.authentication.authentication.AuthenticationService;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.jwt.JwtTokenProvider;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.jwt.helper.JwtConstants;
import com.trading.journal.authentication.user.ApplicationUserService;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final ApplicationUserService applicationUserService;
    private final ReactiveAuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticationServiceImpl(ApplicationUserService applicationUserService,
            ReactiveAuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.applicationUserService = applicationUserService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<LoginResponse> signIn(@Valid Login login) {
        return authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(login.email(), login.password()))
                .onErrorResume(throwable -> Mono.error(new AuthenticationServiceException(throwable.getMessage())))
                .map(Authentication::getPrincipal)
                .cast(UserDetails.class)
                .map(UserDetails::getUsername)
                .flatMap(applicationUserService::getUserByEmail)
                .map(user -> {
                    TokenData tokenData = jwtTokenProvider.generateJwtToken(user);
                    return new LoginResponse(
                            JwtConstants.TOKEN_TYPE,
                            tokenData.accessToken(),
                            "refreshToken",
                            tokenData.expirationIn(),
                            tokenData.issuedAt(),
                            user.firstName());
                });
    }

}
