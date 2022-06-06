package com.trading.journal.authentication.authentication.service;

import com.trading.journal.authentication.authentication.ChangePassword;

public interface PasswordService {
    void requestPasswordChange(String email);

    void changePassword(ChangePassword changePassword);
}
