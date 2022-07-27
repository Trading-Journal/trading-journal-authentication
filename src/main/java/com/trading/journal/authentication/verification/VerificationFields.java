package com.trading.journal.authentication.verification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum VerificationFields {

    REGISTRATION_EMAIL_TEMPLATE("mail/verification.html"),
    CHANGE_PASSWORD_EMAIL_TEMPLATE("mail/change-password.html"),

    ADMIN_REGISTRATION_EMAIL_TEMPLATE("mail/admin-registration.html"),

    NEW_ORGANISATION_USER("mail/organisation-verification.html"),
    USER_NAME("$NAME"),
    URL("$URL"),
    HASH("hash");

    private final String value;
}
