package com.trading.journal.authentication.user.service;

import com.trading.journal.authentication.registration.UserRegistration;

public interface AdminUserService {
    Boolean thereIsAdmin();

    void createAdmin(UserRegistration userRegistration);
}
