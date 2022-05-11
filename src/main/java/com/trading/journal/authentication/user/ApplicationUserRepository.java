package com.trading.journal.authentication.user;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

public interface ApplicationUserRepository extends ReactiveMongoRepository<ApplicationUser, String> {

    Mono<Boolean> existsByUserName(String userName);

    Mono<Boolean> existsByEmail(String email);

    Mono<ApplicationUser> findByEmail(String email);
}