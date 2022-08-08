package com.trading.journal.authentication.user.service;

import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.MeUpdate;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.userauthority.UserAuthorityResponse;

import java.util.List;

public interface UserManagementService {
    PageResponse<UserInfo> getAll(Long tenancyId, PageableRequest pageRequest);

    UserInfo getUserById(Long tenancyId, Long id);

    UserInfo getUserByEmail(Long tenancyId, String email);

    UserInfo create(Long tenancyId, UserRegistration userRegistration);

    void disableUserById(Long tenancyId, Long id);

    void enableUserById(Long tenancyId, Long id);

    void deleteUserById(Long tenancyId, Long id);

    List<UserAuthorityResponse> addAuthorities(Long tenancyId, Long id, AuthoritiesChange authorities);

    List<UserAuthorityResponse> deleteAuthorities(Long tenancyId, Long id, AuthoritiesChange authorities);

    void deleteMeRequest(Long tenancyId, String email);

    void deleteMe(Long tenancyId, String email, String hash);

    UserInfo update(Long tenancyId, String email, MeUpdate meUpdate);
}
