package com.trading.journal.authentication.authority.service;

import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.user.ApplicationUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public interface UserAuthorityService {

    List<UserAuthority> saveCommonUserAuthorities(ApplicationUser applicationUser);

    List<UserAuthority> saveAdminUserAuthorities(ApplicationUser applicationUser);

    List<UserAuthority> getByUserId(Long userId);

    List<SimpleGrantedAuthority> loadListAsSimpleGrantedAuthority(ApplicationUser applicationUser);
}
