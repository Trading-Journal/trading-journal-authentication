package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.service.ApplicationUserManagementService;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationUserManagementServiceImpl implements ApplicationUserManagementService {

    private final ApplicationUserRepository applicationUserRepository;

    private final UserAuthorityService userAuthorityService;

    @Override
    public PageResponse<UserInfo> getAll(PageableRequest pageRequest) {
        Page<ApplicationUser> users;
        users = applicationUserRepository.findAll(pageRequest.pageable());

        List<UserInfo> list = users.stream()
                .peek(applicationUser -> applicationUser.loadAuthorities(userAuthorityService.getByUserId(applicationUser.getId())))
                .map(UserInfo::new).collect(Collectors.toList());
        return new PageResponse<>(users.getTotalElements(), users.getTotalPages(), users.getNumber(), list);
    }

    @Override
    public UserInfo getUserById(Long id) {
        return null;
    }

    @Override
    public void disableUserById(Long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableUserById(Long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteUserById(Long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserInfo changeUserAuthoritiesByUserId(Long id, AuthoritiesChange authorities) {
        return null;
    }
}
