package com.trading.journal.authentication.user.service;

import com.trading.journal.authentication.registration.UserRegistration;

public interface ApplicationAdminUserService {
    Boolean thereIsAdmin();

    void createAdmin(UserRegistration userRegistration);
}
