package com.trading.journal.authentication.verification.service;

import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;

public interface VerificationService {

    void send(VerificationType verificationType, User applicationUser);

    Verification retrieve(String hash);

    void verify(Verification verification);
}
