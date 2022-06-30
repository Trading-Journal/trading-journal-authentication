package com.trading.journal.authentication.email;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailConstants {
    public static final String EMAIL_TEMPLATE = "mail/email.html";
    public static final String MESSAGE_BODY = "$MESSAGE_BODY";
}
