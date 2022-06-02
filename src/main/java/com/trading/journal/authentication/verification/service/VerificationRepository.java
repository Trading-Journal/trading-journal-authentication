package com.trading.journal.authentication.verification.service;

import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface VerificationRepository extends ReactiveCrudRepository<Verification, Long> {

    Mono<Verification> getByHashAndEmail(String hash, String email);

    Mono<Verification> getByTypeAndEmail(VerificationType type, String email);
}
