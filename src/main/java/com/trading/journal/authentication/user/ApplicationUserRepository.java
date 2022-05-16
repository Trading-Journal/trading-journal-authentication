package com.trading.journal.authentication.user;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

public interface ApplicationUserRepository extends ReactiveMongoRepository<ApplicationUser, String> {

    Mono<Boolean> existsByUserName(String userName);

    Mono<Boolean> existsByEmail(String email);

    Mono<ApplicationUser> findByEmail(String email);

    // @Query(value = "{ 'userName' : ?0 }", fields = "{ 'userName' : 1, 'firstName'
    // : 1, 'lastName' : 1, 'email' : 1, 'enabled' : 1, 'verified' : 1,
    // 'authorities' : 1, 'createdAt' : 1}")
    @Query(fields = "{ '_id' : 1, 'firstName' : 1, 'lastName' : 1, 'email' : 1, 'enabled' : 1, 'verified' : 1, 'authorities' : 1, 'createdAt' : 1}")
    Mono<UserInfo> findByUserName(String userName);
}