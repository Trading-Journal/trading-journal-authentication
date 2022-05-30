package com.trading.journal.authentication.email;

import reactor.core.publisher.Mono;

public interface EmailSender {
    Mono<Void> send(EmailRequest request);
}
