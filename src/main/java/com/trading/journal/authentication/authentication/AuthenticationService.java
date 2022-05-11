package com.trading.journal.authentication.authentication;

import reactor.core.publisher.Mono;

public interface AuthenticationService {
    Mono<LoginResponse> signIn(Login login);
}
