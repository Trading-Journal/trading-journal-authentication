package com.trading.journal.authentication.verification.service;

import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.verification.Verification;
import reactor.core.publisher.Mono;

public interface VerificationEmailService {
    Mono<Void> sendEmail(Verification verification, ApplicationUser applicationUser);
}
