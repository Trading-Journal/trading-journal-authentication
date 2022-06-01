package com.trading.journal.authentication.verification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum VerificationFields {

    EMAIL_TEMPLATE("mail/verification.html"),
    USER_NAME("$NAME"),
    URL("$URL"),
    HASH("hash"),
    PATH("authentication/verify");

    private final String value;
}
