package com.trading.journal.authentication.authority.service;

import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.user.ApplicationUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public interface UserAuthorityService {

    UserAuthority saveCommonUserAuthorities(ApplicationUser applicationUser);

    UserAuthority saveAdminUserAuthorities(ApplicationUser applicationUser);

    List<UserAuthority> loadList(Long userId);

    List<SimpleGrantedAuthority> loadListAsSimpleGrantedAuthority(ApplicationUser applicationUser);
}
