package com.trading.journal.authentication.authentication.impl;

import java.util.stream.Collectors;

import javax.validation.Valid;

import com.trading.journal.authentication.authentication.AuthenticationService;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.jwt.JwtConstantsHelper;
import com.trading.journal.authentication.jwt.JwtTokenProvider;
import com.trading.journal.authentication.jwt.TokenData;
import com.trading.journal.authentication.user.ApplicationUserService;
import com.trading.journal.authentication.user.Authority;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
                .then(applicationUserService.getUserByEmail(login.email()))
                .map(user -> {
                    TokenData tokenData = jwtTokenProvider.generateJwtToken(user);
                    return new LoginResponse(
                            JwtConstantsHelper.TOKEN_TYPE,
                            tokenData.token(),
                            user.authorities().stream().map(Authority::name).collect(Collectors.toList()),
                            tokenData.expirationIn(),
                            tokenData.issuedAt(),
                            user.userName(),
                            user.firstName());
                });
    }

}