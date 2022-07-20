package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.service.UserManagementService;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    public static final String MESSAGE = "User id not found";
    private final UserRepository userRepository;

    private final UserAuthorityService userAuthorityService;

    @Override
    public PageResponse<UserInfo> getAll(PageableRequest pageRequest) {
        Specification<User> specification = null;
        if (pageRequest.hasFilter()) {
            specification = filterLike(Columns.USER_NAME, pageRequest.getFilter())
                    .or(filterLike(Columns.FIRST_NAME, pageRequest.getFilter()))
                    .or(filterLike(Columns.LAST_NAME, pageRequest.getFilter()))
                    .or(filterLike(Columns.EMAIL, pageRequest.getFilter()));
        }
        Page<User> users = userRepository.findAll(specification, pageRequest.pageable());
        List<UserInfo> list = users.stream()
                .map(UserInfo::new).collect(Collectors.toList());
        return new PageResponse<>(users.getTotalElements(), users.getTotalPages(), users.getNumber(), list);
    }

    @Override
    public UserInfo getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserInfo::new)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, MESSAGE));
    }

    @Override
    public void disableUserById(Long id) {
        userRepository.findById(id)
                .map(applicationUser -> {
                    applicationUser.disable();
                    return applicationUser;
                })
                .map(userRepository::save)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, MESSAGE));
    }

    @Override
    public void enableUserById(Long id) {
        userRepository.findById(id)
                .map(applicationUser -> {
                    applicationUser.enable();
                    return applicationUser;
                })
                .map(userRepository::save)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, MESSAGE));
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.findById(id)
                .ifPresentOrElse(userRepository::delete, () -> {
                    throw new ApplicationException(HttpStatus.NOT_FOUND, MESSAGE);
                });
    }

    @Override
    public List<UserAuthority> addAuthorities(Long id, AuthoritiesChange authorities) {
        return userRepository.findById(id)
                .map(applicationUser -> userAuthorityService.addAuthorities(applicationUser, authorities))
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, MESSAGE));
    }

    @Override
    public List<UserAuthority> deleteAuthorities(Long id, AuthoritiesChange authorities) {
        return userRepository.findById(id)
                .map(applicationUser -> userAuthorityService.deleteAuthorities(applicationUser, authorities))
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, MESSAGE));
    }

    private Specification<User> filterLike(String column, String value) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.like(root.get(column), "%" + value.toLowerCase(Locale.getDefault()) + "%");
    }

    private static class Columns {
        public static final String USER_NAME = "userName";
        public static final String FIRST_NAME = "firstName";
        public static final String LAST_NAME = "lastName";
        public static final String EMAIL = "email";
    }
}
