package com.trading.journal.authentication.password.service;

public interface PasswordService {
    String encodePassword(String rawPassword);

    String randomPassword();
}
