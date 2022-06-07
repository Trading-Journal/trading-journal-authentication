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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationUserServiceImpl implements ApplicationUserService {

    private final ApplicationUserRepository applicationUserRepository;

    private final UserAuthorityService userAuthorityService;

    private final PasswordEncoder encoder;

    private final VerificationProperties verificationProperties;

    @Override
    public UserDetails loadUserByUsername(String username) {
        ApplicationUser applicationUser = this.getUserByEmail(username);
        List<SimpleGrantedAuthority> authorities = userAuthorityService.loadListAsSimpleGrantedAuthority(applicationUser);
        if (authorities == null || authorities.isEmpty()) {
            throw new ApplicationException("There is no authorities for this user");
        }
        return User.withUsername(applicationUser.getEmail())
                .password(applicationUser.getPassword())
                .authorities(authorities)
                .accountExpired(!applicationUser.getEnabled())
                .credentialsExpired(!applicationUser.getVerified())
                .disabled(!applicationUser.getEnabled())
                .accountLocked(!applicationUser.getVerified())
                .build();
    }

    @Override
    public ApplicationUser getUserByEmail(@NotBlank String email) {
        ApplicationUser applicationUser = applicationUserRepository.findByEmail(email);
        if (applicationUser == null) {
            throw new UsernameNotFoundException(String.format("User %s does not exist", email));
        }
        List<UserAuthority> authorities = userAuthorityService.getByUserId(applicationUser.getId());
        applicationUser.loadAuthorities(authorities);
        return applicationUser;
    }

    @Override
    public ApplicationUser createNewUser(@NotNull UserRegistration userRegistration) {
        Boolean validUser = validateNewUser(userRegistration.userName(), userRegistration.email());
        if (validUser) {
            ApplicationUser applicationUser = applicationUserRepository.save(user(userRegistration));
            userAuthorityService.saveCommonUserAuthorities(applicationUser);
            return applicationUser;
        } else {
            throw new ApplicationException("User name or email already exist");
        }
    }

    @Override
    public Boolean validateNewUser(@NotNull String userName, @NotBlank String email) {
        Boolean userNameExists = userNameExists(userName);
        Boolean emailExists = emailExists(email);
        return !userNameExists && !emailExists;
    }

    @Override
    public Boolean userNameExists(@NotBlank String userName) {
        return applicationUserRepository.existsByUserName(userName);
    }

    @Override
    public Boolean emailExists(@NotBlank String email) {
        return applicationUserRepository.existsByEmail(email);
    }

    @Override
    public UserInfo getUserInfo(@NotBlank String userName) {
        return applicationUserRepository.getUserInfoByUserName(userName);
    }

    @Override
    public void verifyNewUser(@NotBlank String email) {
        ApplicationUser applicationUser = this.getUserByEmail(email);
        applicationUser.enable();
        applicationUser.verify();
        applicationUserRepository.save(applicationUser);
    }

    @Override
    public ApplicationUser changePassword(@NotBlank String email, @NotBlank String password) {
        ApplicationUser applicationUser = this.getUserByEmail(email);
        applicationUser.changePassword(encoder.encode(password));
        return applicationUserRepository.save(applicationUser);
    }

    private ApplicationUser user(UserRegistration userRegistration) {
        boolean enabledAndVerified = !verificationProperties.isEnabled();
        return ApplicationUser.builder()
                .userName(userRegistration.userName())
                .password(encoder.encode(userRegistration.password()))
                .firstName(userRegistration.firstName())
                .lastName(userRegistration.lastName())
                .email(userRegistration.email())
                .enabled(enabledAndVerified)
                .verified(enabledAndVerified)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
