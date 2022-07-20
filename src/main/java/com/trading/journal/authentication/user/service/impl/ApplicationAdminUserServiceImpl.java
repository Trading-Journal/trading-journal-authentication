package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.authority.AuthoritiesHelper;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.password.service.PasswordService;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.user.service.ApplicationAdminUserService;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationAdminUserServiceImpl implements ApplicationAdminUserService {

    private final ApplicationUserRepository applicationUserRepository;

    private final UserAuthorityService userAuthorityService;

    private final VerificationService verificationService;

    private final PasswordService passwordService;

    @Override
    public Boolean thereIsAdmin() {
        List<String> roles = AuthoritiesHelper.getByCategory(AuthorityCategory.ADMINISTRATOR).stream().map(AuthoritiesHelper::getLabel).toList();
        Integer admins = applicationUserRepository.countAdmins(roles);
        return admins > 0;
    }

    @Override
    public void createAdmin(@Valid UserRegistration userRegistration) {
        User applicationUser = applicationUserRepository.save(adminUser(userRegistration));
        userAuthorityService.saveAdminUserAuthorities(applicationUser);
        verificationService.send(VerificationType.ADMIN_REGISTRATION, applicationUser);
    }

    private User adminUser(UserRegistration userRegistration) {
        return User.builder()
                .userName(userRegistration.userName())
                .password(passwordService.randomPassword())
                .firstName(userRegistration.firstName())
                .lastName(userRegistration.lastName())
                .email(userRegistration.email())
                .enabled(false)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
