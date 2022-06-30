package com.trading.journal.authentication.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailConstantsTest {
    @DisplayName("Check constants values")
    @Test
    void check() {
        assertThat(EmailConstants.EMAIL_TEMPLATE).isEqualTo("mail/email.html");
        assertThat(EmailConstants.MESSAGE_BODY).isEqualTo("$MESSAGE_BODY");
    }
}