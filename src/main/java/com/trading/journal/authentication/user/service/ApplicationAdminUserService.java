package com.trading.journal.authentication.user.service;

import com.trading.journal.authentication.registration.AdminRegistration;
import reactor.core.publisher.Mono;

public interface ApplicationAdminUserService {
    Mono<Boolean> thereIsAdmin();

    Mono<Void> createAdmin(AdminRegistration adminRegistration);
}
