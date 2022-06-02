package com.trading.journal.authentication.user.service;

import javax.validation.constraints.NotNull;

import com.trading.journal.authentication.registration.UserRegistration;

import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.UserInfo;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public interface ApplicationUserService extends ReactiveUserDetailsService {

    Mono<ApplicationUser> getUserByEmail(String email);

    Mono<ApplicationUser> createNewUser(@NotNull UserRegistration userRegistration);

    Mono<Boolean> validateNewUser(@NotNull String userName, String email);

    Mono<Boolean> userNameExists(String userName);

    Mono<Boolean> emailExists(String email);

    Mono<UserInfo> getUserInfo(String userName);

    Mono<Void> verifyNewUser(String email);
}
