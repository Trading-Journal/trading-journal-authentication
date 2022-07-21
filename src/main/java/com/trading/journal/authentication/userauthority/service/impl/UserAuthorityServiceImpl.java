package com.trading.journal.authentication.userauthority.service.impl;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.service.AuthorityService;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAuthorityServiceImpl implements UserAuthorityService {

    private final UserAuthorityRepository userAuthorityRepository;

    private final AuthorityService authorityService;

    @Override
    public List<UserAuthority> saveCommonUserAuthorities(User applicationUser) {
        return authorityService.getAuthoritiesByCategory(AuthorityCategory.COMMON_USER)
                .stream()
                .map(authority -> new UserAuthority(applicationUser, authority))
                .map(userAuthorityRepository::save)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAuthority> saveAdminUserAuthorities(User applicationUser) {
        return authorityService.getAll()
                .stream()
                .map(authority -> new UserAuthority(applicationUser, authority))
                .map(userAuthorityRepository::save)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAuthority> addAuthorities(User applicationUser, AuthoritiesChange authoritiesChange) {
        List<Authority> authorities = authoritiesChange.authorities().stream()
                .map(authorityService::getByName)
                .filter(Optional::isPresent)
                .map(Optional::get).toList();

        List<UserAuthority> userAuthoritiesToAdd = authorities.stream()
                .filter(filterOutEqualAuthorities(applicationUser))
                .map(authority -> new UserAuthority(applicationUser, authority))
                .toList();

        return userAuthoritiesToAdd.stream()
                .map(userAuthorityRepository::save)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAuthority> deleteAuthorities(User applicationUser, AuthoritiesChange authoritiesChange) {
        List<Authority> authorities = authoritiesChange.authorities().stream()
                .map(authorityService::getByName)
                .filter(Optional::isPresent)
                .map(Optional::get).toList();

        List<UserAuthority> userAuthoritiesToRemove = applicationUser.getAuthorities()
                .stream()
                .filter(filterUserRolesToRemove(authorities)).toList();

        userAuthoritiesToRemove.forEach(userAuthorityRepository::delete);
        return userAuthoritiesToRemove;
    }

    private Predicate<Authority> filterOutEqualAuthorities(User applicationUser) {
        return authority -> applicationUser
                .getAuthorities()
                .stream()
                .noneMatch(userAuthority -> userAuthority.getAuthority().getName().equals(authority.getName())
                        && Objects.equals(userAuthority.getAuthority().getId(), authority.getId()));
    }

    private Predicate<UserAuthority> filterUserRolesToRemove(List<Authority> authorities) {
        return userAuthority -> authorities.stream()
                .anyMatch(authority -> userAuthority.getAuthority().getName().equals(authority.getName())
                        && Objects.equals(userAuthority.getAuthority().getId(), authority.getId())
                );
    }
}
