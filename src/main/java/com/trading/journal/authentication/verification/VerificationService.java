package com.trading.journal.authentication.verification;

import com.trading.journal.authentication.user.ApplicationUser;
import reactor.core.publisher.Mono;

public interface VerificationService {

    Mono<Void> send(VerificationType verificationType, ApplicationUser applicationUser);

    Mono<Verification> retrieve(String hash, String email);

    Mono<Void> verify(Verification verification);
}
