package com.trading.journal.authentication.user.service;

import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.UserInfo;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApplicationUserRepository extends ReactiveCrudRepository<ApplicationUser, Long> {

    Mono<Integer> countByUserName(String userName);

    Mono<Integer> countByEmail(String email);

    Mono<ApplicationUser> findByEmail(String email);

    @Query("select id, userName, firstName, lastName, email, enabled, verified, createdAt from Users where userName = :userName")
    Mono<UserInfo> findByUserName(String userName);

    @Query("SELECT COUNT(Users.id) FROM Users inner join UserAuthorities where Users.id = UserAuthorities.userId and UserAuthorities.name in (:roles)")
    Mono<Integer> countAdmins(List<String> roles);
}