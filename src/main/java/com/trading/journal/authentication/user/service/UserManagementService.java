package com.trading.journal.authentication.user.service;

import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.userauthority.UserAuthority;

import java.util.List;

public interface UserManagementService {
    PageResponse<UserInfo> getAll(Long tenancyId, PageableRequest pageRequest);

    UserInfo getUserById(Long tenancyId, Long id);

    void disableUserById(Long tenancyId, Long id);

    void enableUserById(Long tenancyId, Long id);

    void deleteUserById(Long tenancyId, Long id);

    List<UserAuthority> addAuthorities(Long tenancyId, Long id, AuthoritiesChange authorities);

    List<UserAuthority> deleteAuthorities(Long tenancyId, Long id, AuthoritiesChange authorities);
}
