package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.pageable.specifications.FilterLike;
import com.trading.journal.authentication.pageable.specifications.FilterTenancy;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.service.TenancyService;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.UserManagementRepository;
import com.trading.journal.authentication.user.service.UserManagementService;
import com.trading.journal.authentication.user.service.UserService;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    public static final String MESSAGE = "User not found";
    private final UserManagementRepository userManagementRepository;

    private final UserService userService;

    private final VerificationService verificationService;

    private final TenancyService tenancyService;

    private final UserAuthorityService userAuthorityService;

    @Override
    public PageResponse<UserInfo> getAll(Long tenancyId, PageableRequest pageRequest) {
        Specification<User> specification = new FilterTenancy<User>(tenancyId).apply();
        if (pageRequest.hasFilter()) {
            Specification<User> filter = new FilterLike<User>(pageRequest.getFilter()).apply(Columns.USER_NAME)
                    .or(new FilterLike<User>(pageRequest.getFilter()).apply(Columns.USER_NAME))
                    .or(new FilterLike<User>(pageRequest.getFilter()).apply(Columns.FIRST_NAME))
                    .or(new FilterLike<User>(pageRequest.getFilter()).apply(Columns.LAST_NAME))
                    .or(new FilterLike<User>(pageRequest.getFilter()).apply(Columns.EMAIL));
            specification = specification.and(filter);
        }
        Page<User> users = userManagementRepository.findAll(specification, pageRequest.pageable());
        List<UserInfo> list = users.stream().map(UserInfo::new).collect(Collectors.toList());
        return new PageResponse<>(users.getTotalElements(), users.getTotalPages(), users.getNumber(), list);
    }

    @Override
    public UserInfo getUserById(Long tenancyId, Long id) {
        User user = getUser(tenancyId, id);
        return new UserInfo(user);
    }

    @Override
    public UserInfo create(Long tenancyId, UserRegistration userRegistration) {
        Tenancy tenancy = tenancyService.getById(tenancyId);
        if (tenancy.increaseUsageAllowed()) {
            userRegistration.randomPassword();
            User user = userService.createNewUser(userRegistration, tenancy);
            verificationService.send(VerificationType.NEW_ORGANISATION_USER, user);
            tenancyService.increaseUsage(tenancy.getId());
            return new UserInfo(user);
        }
        throw new ApplicationException("Tenancy has reach its user limit");
    }

    @Override
    public void disableUserById(Long tenancyId, Long id) {
        User user = getUser(tenancyId, id);
        user.disable();
        userManagementRepository.save(user);
    }

    @Override
    public void enableUserById(Long tenancyId, Long id) {
        User user = getUser(tenancyId, id);
        user.enable();
        userManagementRepository.save(user);
    }

    @Override
    public void deleteUserById(Long tenancyId, Long id) {
        User user = getUser(tenancyId, id);
        userManagementRepository.delete(user);
        tenancyService.lowerUsage(tenancyId);
    }

    @Override
    public List<UserAuthority> addAuthorities(Long tenancyId, Long id, AuthoritiesChange authorities) {
        User user = getUser(tenancyId, id);
        return userAuthorityService.addAuthorities(user, authorities);
    }

    @Override
    public List<UserAuthority> deleteAuthorities(Long tenancyId, Long id, AuthoritiesChange authorities) {
        User user = getUser(tenancyId, id);
        return userAuthorityService.deleteAuthorities(user, authorities);
    }

    @Override
    public void deleteMeRequest(Long tenancyId, String email) {
        userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)
                .ifPresentOrElse(user -> verificationService.send(VerificationType.DELETE_ME, user),
                        () -> {
                            throw new ApplicationException(HttpStatus.BAD_REQUEST, String.format("Email %s not found", email));
                        });
    }

    @Override
    public void deleteMe(Long tenancyId, String email, String hash) {
        Verification verification = verificationService.retrieve(hash);
        if (!Objects.equals(verification.getEmail(), email)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Verification does not exist or is invalid");
        }
        User user = userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, String.format("Email %s not found", email)));

        deleteUserById(tenancyId, user.getId());
        verificationService.verify(verification);
        Boolean thereIsUsers = userService.existsByTenancyId(tenancyId);
        if (!thereIsUsers) {
            tenancyService.delete(tenancyId);
        }
    }

    private User getUser(Long tenancyId, Long id) {
        return userManagementRepository.findByTenancyIdAndId(tenancyId, id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, MESSAGE));
    }

    private static class Columns {
        public static final String USER_NAME = "userName";
        public static final String FIRST_NAME = "firstName";
        public static final String LAST_NAME = "lastName";
        public static final String EMAIL = "email";
    }
}
