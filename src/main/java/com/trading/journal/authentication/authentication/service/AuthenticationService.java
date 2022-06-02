package com.trading.journal.authentication.authentication.service;

import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import reactor.core.publisher.Mono;

public interface AuthenticationService {
    Mono<LoginResponse> signIn(Login login);

    Mono<LoginResponse> refreshToken(String refreshToken);
}
