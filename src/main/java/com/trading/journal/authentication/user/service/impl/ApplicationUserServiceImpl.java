package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.authority.service.UserAuthorityService;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.service.ApplicationUserRepository;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.verification.properties.VerificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
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
                .switchIfEmpty(Mono.error(userNotFound(email)))
                .flatMap(applicationUser ->
                        userAuthorityService.loadListAsSimpleGrantedAuthority(applicationUser)
                                .doOnSuccess(authorities -> ofNullable(authorities)
                                        .filter(list -> !list.isEmpty())
                                        .orElseThrow(() -> new ApplicationException("There is no authorities for this user")))
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
    public Mono<ApplicationUser> getUserByEmail(@NotBlank String email) {
        return applicationUserRepository
                .findByEmail(email)
                .zipWhen(userAuthorityService::loadList)
                .switchIfEmpty(Mono.error(userNotFound(email)))
                .map(userAndAuthorities -> {
                    ApplicationUser applicationUser = userAndAuthorities.getT1();
                    applicationUser.loadAuthorities(userAndAuthorities.getT2());
                    return applicationUser;
                });
    }

    @Override
    public Mono<ApplicationUser> createNewUser(@NotNull UserRegistration userRegistration) {
        return validateNewUser(userRegistration.userName(), userRegistration.email())
                .doOnSuccess(valid -> {
                    if (!valid) {
                        throw new ApplicationException("User name or email already exist");
                    }
                })
                .onErrorResume(Mono::error)
                .then(buildNewUser(userRegistration))
                .flatMap(applicationUserRepository::save)
                .flatMap(userAuthorityService::saveCommonUserAuthorities)
                .map(UserAuthority::getUserId)
                .flatMap(applicationUserRepository::findById)
                .name("create_new_user").metrics();
    }

    @Override
    public Mono<Boolean> validateNewUser(@NotNull String userName, @NotBlank String email) {
        Mono<Boolean> userNameExists = userNameExists(userName);
        Mono<Boolean> emailExists = emailExists(email);
        return userNameExists.zipWith(emailExists).map(result -> !result.getT1() && !result.getT2());
    }

    @Override
    public Mono<Boolean> userNameExists(@NotBlank String userName) {
        return applicationUserRepository.countByUserName(userName).map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> emailExists(@NotBlank String email) {
        return applicationUserRepository.countByEmail(email).map(count -> count > 0);
    }

    @Override
    public Mono<UserInfo> getUserInfo(@NotBlank String userName) {
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
    public Mono<Void> verifyNewUser(@NotBlank String email) {
        return applicationUserRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(userNotFound(email)))
                .map(applicationUser -> {
                    applicationUser.enable();
                    applicationUser.verify();
                    return applicationUser;
                })
                .flatMap(applicationUserRepository::save)
                .then();
    }

    @Override
    public Mono<ApplicationUser> changePassword(@NotBlank String email, @NotBlank String password) {
        return applicationUserRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(userNotFound(email)))
                .map(applicationUser -> {
                    applicationUser.changePassword(encoder.encode(password));
                    return applicationUser;
                })
                .flatMap(applicationUserRepository::save);
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

    private UsernameNotFoundException userNotFound(String email) {
        return new UsernameNotFoundException(String.format("User %s does not exist", email));
    }
}
