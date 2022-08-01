package com.trading.journal.authentication.verification;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationRepository extends CrudRepository<Verification, Long> {

    @Override
    List<Verification> findAll();

    Optional<Verification> getByHashAndEmail(String hash, String email);

    Optional<Verification> getByTypeAndEmail(VerificationType type, String email);

    List<Verification> getByEmail(String email);
}
