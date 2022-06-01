package com.trading.journal.authentication.verification.service;

public interface HashProvider {

    String generateHash(String value);

    String readHashValue(String hash);
}
