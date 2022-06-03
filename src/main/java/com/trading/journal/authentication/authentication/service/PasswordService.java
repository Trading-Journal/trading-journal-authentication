package com.trading.journal.authentication.authentication.service;

import com.trading.journal.authentication.authentication.ChangePassword;
import reactor.core.publisher.Mono;

public interface PasswordService {
    Mono<Void> requestPasswordChange(String email);

    Mono<Void> changePassword(ChangePassword changePassword);
}
