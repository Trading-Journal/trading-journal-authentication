package com.trading.journal.authentication.password.service;

import com.trading.journal.authentication.password.ChangePassword;

public interface PasswordManagementService {
    void requestPasswordChange(String email);

    void changePassword(ChangePassword changePassword);
}
