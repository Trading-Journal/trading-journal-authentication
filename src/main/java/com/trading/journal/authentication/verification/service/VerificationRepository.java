package com.trading.journal.authentication.verification.service;

import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface VerificationRepository extends CrudRepository<Verification, Long> {

    @Override
    List<Verification> findAll();

    Verification getByHashAndEmail(String hash, String email);

    Verification getByTypeAndEmail(VerificationType type, String email);
}
