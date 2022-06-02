package com.trading.journal.authentication.verification.service;

import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import reactor.core.publisher.Mono;

public interface VerificationService {

    Mono<Void> send(VerificationType verificationType, ApplicationUser applicationUser);

    Mono<Verification> retrieve(String hash);

    Mono<Void> verify(Verification verification);
}
