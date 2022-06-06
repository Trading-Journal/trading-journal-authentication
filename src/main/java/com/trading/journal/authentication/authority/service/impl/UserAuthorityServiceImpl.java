package com.trading.journal.authentication.authority.service.impl;

import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.authority.service.AuthorityService;
import com.trading.journal.authentication.authority.service.UserAuthorityRepository;
import com.trading.journal.authentication.authority.service.UserAuthorityService;
import com.trading.journal.authentication.user.ApplicationUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAuthorityServiceImpl implements UserAuthorityService {

    private final UserAuthorityRepository userAuthorityRepository;

    private final AuthorityService authorityService;

    @Override
    public UserAuthority saveCommonUserAuthorities(ApplicationUser applicationUser) {
        return authorityService.getAuthoritiesByCategory(AuthorityCategory.COMMON_USER)
                .stream()
                .map(authority -> new UserAuthority(applicationUser.getId(), authority.getName(), authority.getId()))
                .map(userAuthorityRepository::save)
                .findFirst()
                .orElse(null);
    }

    @Override
    public UserAuthority saveAdminUserAuthorities(ApplicationUser applicationUser) {
        return authorityService.getAll()
                .stream()
                .map(authority -> new UserAuthority(applicationUser.getId(), authority.getName(), authority.getId()))
                .map(userAuthorityRepository::save)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<UserAuthority> loadList(Long userId) {
        return userAuthorityRepository.findByUserId(userId);
    }

    @Override
    public List<SimpleGrantedAuthority> loadListAsSimpleGrantedAuthority(ApplicationUser applicationUser) {
        return userAuthorityRepository.findByUserId(applicationUser.getId())
                .stream()
                .map(userAuthorities -> new SimpleGrantedAuthority(userAuthorities.getName()))
                .collect(Collectors.toList());
    }
}
