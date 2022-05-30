package com.trading.journal.authentication.user.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.authority.UserAuthorityService;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.user.ApplicationUserService;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.verification.properties.VerificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class ApplicationUserServiceImpl implements ApplicationUserService {

    private final ApplicationUserRepository applicationUserRepository;

    private final UserAuthorityService userAuthorityService;

    private final PasswordEncoder encoder;

    private final VerificationProperties verificationProperties;

    @Override
    public Mono<UserDetails> findByUsername(String email) {
        return applicationUserRepository
                .findByEmail(email)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException(String.format("User %s does not exist", email))))
                .flatMap(applicationUser ->
                        userAuthorityService.loadListAsSimpleGrantedAuthority(applicationUser)
                                .doOnSuccess(checkForEmptyAuthorities())
                                .onErrorResume(Mono::error)
                                .map(simpleGrantedAuthorities -> User.withUsername(applicationUser.getEmail())
                                        .password(applicationUser.getPassword())
                                        .authorities(simpleGrantedAuthorities)
                                        .accountExpired(!applicationUser.getEnabled())
                                        .credentialsExpired(!applicationUser.getEnabled())
                                        .disabled(!applicationUser.getEnabled())
                                        .accountLocked(!applicationUser.getVerified())
                                        .build())
                );
    }

    @Override
    public Mono<ApplicationUser> getUserByEmail(String email) {
        return applicationUserRepository
                .findByEmail(email)
                .zipWhen(userAuthorityService::loadList)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException(String.format("User %s does not exist", email))))
                .map(userAndAuthorities -> {
                    ApplicationUser applicationUser = userAndAuthorities.getT1();
                    applicationUser.loadAuthorities(userAndAuthorities.getT2());
                    return applicationUser;
                });
    }

    @Override
    public Mono<ApplicationUser> createNewUser(@NotNull UserRegistration userRegistration) {
        return validateNewUser(userRegistration.userName(), userRegistration.email())
                .doOnSuccess(checkForInvalidUser())
                .onErrorResume(Mono::error)
                .then(buildNewUser(userRegistration))
                .flatMap(applicationUserRepository::save)
                .flatMap(userAuthorityService::saveCommonUserAuthorities)
                .map(UserAuthority::getUserId)
                .flatMap(applicationUserRepository::findById)
                .name("create_new_user").metrics();
    }

    @Override
    public Mono<Boolean> validateNewUser(@NotNull String userName, String email) {
        Mono<Boolean> userNameExists = userNameExists(userName);
        Mono<Boolean> emailExists = emailExists(email);
        return userNameExists.zipWith(emailExists).map(result -> !result.getT1() && !result.getT2());
    }

    @Override
    public Mono<Boolean> userNameExists(String userName) {
        return applicationUserRepository.countByUserName(userName).map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> emailExists(String email) {
        return applicationUserRepository.countByEmail(email).map(count -> count > 0);
    }

    @Override
    public Mono<UserInfo> getUserInfo(String userName) {
        return applicationUserRepository.findByUserName(userName)
                .zipWhen(userInfo -> userAuthorityService.loadList(userInfo.getId()))
                .map(userInfoAndAuthorities -> {
                    UserInfo userInfo = userInfoAndAuthorities.getT1();
                    userInfo.loadAuthorities(userInfoAndAuthorities.getT2().stream().map(UserAuthority::getName).collect(Collectors.toList()));
                    return userInfo;
                })
                .name("get_me_info").metrics();
    }

    @Override
    public Mono<Void> verifyNewUser(String email) {
        return applicationUserRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException(String.format("User %s does not exist", email))))
                .map(applicationUser -> {
                    applicationUser.enable();
                    applicationUser.verify();
                    return applicationUser;
                })
                .flatMap(applicationUserRepository::save)
                .then();
    }

    private Consumer<List<SimpleGrantedAuthority>> checkForEmptyAuthorities() {
        return authorities -> ofNullable(authorities)
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

    private Mono<ApplicationUser> buildNewUser(UserRegistration userRegistration) {
        boolean enabledAndVerified = !verificationProperties.isEnabled();
        return Mono.just(ApplicationUser.builder()
                .userName(userRegistration.userName())
                .password(encoder.encode(userRegistration.password()))
                .firstName(userRegistration.firstName())
                .lastName(userRegistration.lastName())
                .email(userRegistration.email())
                .enabled(enabledAndVerified)
                .verified(enabledAndVerified)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
