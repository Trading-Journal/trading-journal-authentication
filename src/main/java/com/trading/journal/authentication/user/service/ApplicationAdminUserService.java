package com.trading.journal.authentication.user.service;

import com.trading.journal.authentication.registration.UserRegistration;
import reactor.core.publisher.Mono;

public interface ApplicationAdminUserService {
    Mono<Boolean> thereIsAdmin();

    Mono<Void> createAdmin(UserRegistration userRegistration);
}
