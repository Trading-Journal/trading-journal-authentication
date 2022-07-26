package com.trading.journal.authentication.userauthority.service;

import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.userauthority.UserAuthority;

import java.util.List;

public interface UserAuthorityService {

    List<UserAuthority> saveCommonUserAuthorities(User applicationUser);

    List<UserAuthority> saveAdminUserAuthorities(User applicationUser);

    List<UserAuthority> saveOrganisationAdminUserAuthorities(User applicationUser);

    List<UserAuthority> addAuthorities(User applicationUser, AuthoritiesChange authorities);

    List<UserAuthority> deleteAuthorities(User applicationUser, AuthoritiesChange authorities);
}
