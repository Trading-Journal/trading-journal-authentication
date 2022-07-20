package com.trading.journal.authentication.verification.service;

import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.verification.Verification;

public interface VerificationEmailService {
    void sendEmail(Verification verification, User applicationUser);
}
