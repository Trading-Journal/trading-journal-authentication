package com.trading.journal.authentication.email.service;

import com.trading.journal.authentication.email.EmailRequest;
import reactor.core.publisher.Mono;

public interface EmailSender {
    Mono<Void> send(EmailRequest request);
}
