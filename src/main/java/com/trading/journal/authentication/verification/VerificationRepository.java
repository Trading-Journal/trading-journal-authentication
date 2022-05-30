package com.trading.journal.authentication.verification;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface VerificationRepository extends ReactiveCrudRepository<Verification, Long> {

    Mono<Verification> getByHashAndEmail(String hash, String email);

    Mono<Verification> getByTypeAndEmail(VerificationType type, String email);
}
