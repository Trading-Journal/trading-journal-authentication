package com.trading.journal.authentication.user.impl;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.configuration.AuthoritiesHelper;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.user.ApplicationUserService;
import com.trading.journal.authentication.user.Authority;
import com.trading.journal.authentication.user.UserInfo;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class ApplicationUserServiceImpl implements ApplicationUserService {

    private final ApplicationUserRepository repository;
    private final PasswordEncoder encoder;

    public ApplicationUserServiceImpl(ApplicationUserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public Mono<UserDetails> findByUsername(String email) {
        return repository
                .findByEmail(email)
                .switchIfEmpty(
                        Mono.error(new UsernameNotFoundException(String.format("User %s does not exist", email))))
                .doOnSuccess(checkForEmptyAuthorities())
                .onErrorResume(Mono::error)
                .map(user -> User.withUsername(user.email())
                        .password(user.password())
                        .authorities(user.authorities().stream()
                                .map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toList()))
                        .accountExpired(!user.enabled())
                        .credentialsExpired(!user.enabled())
                        .disabled(!user.enabled())
                        .accountLocked(!user.verified())
                        .build());
    }

    @Override
    public Mono<ApplicationUser> getUserByEmail(String email) {
        return repository
                .findByEmail(email)
                .switchIfEmpty(
                        Mono.error(new UsernameNotFoundException(String.format("User %s does not exist", email))));
    }

    @Override
    public Mono<ApplicationUser> createNewUser(@NotNull UserRegistration userRegistration) {
        return validateNewUser(userRegistration.userName(), userRegistration.email())
                .doOnSuccess(checkForInvalidUser())
                .onErrorResume(Mono::error)
                .flatMap(a -> repository.save(buildNewUser(userRegistration)));
    }

    @Override
    public Mono<Boolean> validateNewUser(@NotNull String userName, String email) {
        Mono<Boolean> userNameExists = userNameExists(userName);
        Mono<Boolean> emailExists = emailExists(email);
        return userNameExists.zipWith(emailExists).map(result -> !result.getT1() && !result.getT2());
    }

    @Override
    public Mono<Boolean> userNameExists(String userName) {
        return repository.existsByUserName(userName);
    }

    @Override
    public Mono<Boolean> emailExists(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Mono<UserInfo> getUserInfo(String userName) {
        return repository.findByUserName(userName);
    }

    private Consumer<ApplicationUser> checkForEmptyAuthorities() {
        return user -> ofNullable(user.authorities())
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new ApplicationException("There is no authorities for this user"));
    }

    private Consumer<Boolean> checkForInvalidUser() {
        return valid -> {
            if (!valid) {
                throw new ApplicationException("User name or email already exist");
            }
        };
    }

    private ApplicationUser buildNewUser(UserRegistration userRegistration) {
        return new ApplicationUser(userRegistration.userName(),
                encoder.encode(userRegistration.password()),
                userRegistration.firstName(),
                userRegistration.lastName(),
                userRegistration.email(),
                true,
                true,
                singletonList(new Authority(AuthoritiesHelper.ROLE_USER)),
                LocalDateTime.now());
    }

}
