package com.trading.journal.authentication.registration.service;

import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import reactor.core.publisher.Mono;

public interface RegistrationService {
    Mono<SignUpResponse> signUp(UserRegistration userRegistration);

    Mono<Void> verify(String hash);
}
