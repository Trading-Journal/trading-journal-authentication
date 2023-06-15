package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.authority.AuthoritiesHelper;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.password.service.PasswordService;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.user.service.AdminUserService;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    private final UserAuthorityService userAuthorityService;

    private final VerificationService verificationService;

    private final PasswordService passwordService;

    @Override
    public Boolean thereIsAdmin() {
        List<String> roles = AuthoritiesHelper.getByCategory(AuthorityCategory.ADMINISTRATOR).stream().map(AuthoritiesHelper::getLabel).toList();
        Integer admins = userRepository.countAdmins(roles);
        return admins > 0;
    }

    @Override
    public void createAdmin(UserRegistration userRegistration) {
        User applicationUser = userRepository.save(adminUser(userRegistration));
        userAuthorityService.saveAdminUserAuthorities(applicationUser);
        verificationService.send(VerificationType.ADMIN_REGISTRATION, applicationUser);
    }

    private User adminUser(UserRegistration userRegistration) {
        return User.builder()
                .password(passwordService.randomPassword())
                .firstName(userRegistration.getFirstName())
                .lastName(userRegistration.getLastName())
                .email(userRegistration.getEmail())
                .enabled(false)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
