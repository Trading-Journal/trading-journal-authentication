package com.trading.journal.authentication.registration.service;

import com.trading.journal.authentication.registration.UserRegistration;

import reactor.core.publisher.Mono;

public interface SignupService {
    Mono<Void> signUp(UserRegistration userRegistration);
}
