package com.trading.journal.authentication.verification;

import com.trading.journal.authentication.user.ApplicationUser;
import reactor.core.publisher.Mono;

public interface VerificationEmailService {
    Mono<Void> sendEmail(Verification verification, ApplicationUser applicationUser);
}
