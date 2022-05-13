package com.trading.journal.authentication.registration.service;

import com.trading.journal.authentication.registration.UserRegistration;

import reactor.core.publisher.Mono;

public interface RegistrationService {
    Mono<Void> signUp(UserRegistration userRegistration);
}
