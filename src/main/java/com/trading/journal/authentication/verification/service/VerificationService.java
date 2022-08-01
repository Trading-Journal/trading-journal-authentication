package com.trading.journal.authentication.verification.service;

import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationRequest;
import com.trading.journal.authentication.verification.VerificationType;

import java.util.List;

public interface VerificationService {

    void send(VerificationType verificationType, User applicationUser);

    Verification retrieve(String hash);

    void verify(Verification verification);

    List<Verification> getByEmail(String email);

    Verification create(VerificationRequest verificationRequest);
}
