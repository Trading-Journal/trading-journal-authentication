package com.trading.journal.authentication.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class HomeController {

    @GetMapping
    public Mono<String> home() {
        return Mono.just("Hello World!");
    }
}
