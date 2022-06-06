package com.trading.journal.authentication.email.service;

import com.trading.journal.authentication.email.EmailRequest;

public interface EmailSender {
    void send(EmailRequest request);
}
