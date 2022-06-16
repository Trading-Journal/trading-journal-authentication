package com.trading.journal.authentication.user.service;

import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.userauthority.UserAuthority;

import java.util.List;

public interface ApplicationUserManagementService {
    PageResponse<UserInfo> getAll(PageableRequest pageRequest);

    UserInfo getUserById(Long id);

    void disableUserById(Long id);

    void enableUserById(Long id);

    void deleteUserById(Long id);

    List<UserAuthority> addAuthorities(Long id, AuthoritiesChange authorities);

    List<UserAuthority> deleteAuthorities(Long id, AuthoritiesChange authorities);
}
