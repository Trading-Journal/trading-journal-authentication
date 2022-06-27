package com.trading.journal.authentication.userauthority.service.impl;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import com.trading.journal.authentication.authority.service.AuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    public List<UserAuthority> saveCommonUserAuthorities(ApplicationUser applicationUser) {
        return authorityService.getAuthoritiesByCategory(AuthorityCategory.COMMON_USER)
                .stream()
                .map(authority -> new UserAuthority(applicationUser.getId(), authority.getName(), authority.getId()))
                .map(userAuthorityRepository::save)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAuthority> saveAdminUserAuthorities(ApplicationUser applicationUser) {
        return authorityService.getAll()
                .stream()
                .map(authority -> new UserAuthority(applicationUser.getId(), authority.getName(), authority.getId()))
                .map(userAuthorityRepository::save)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAuthority> addAuthorities(ApplicationUser applicationUser, AuthoritiesChange authoritiesChange) {
        List<Authority> authorities = authoritiesChange.authorities().stream()
                .map(authorityService::getByName)
                .filter(Optional::isPresent)
                .map(Optional::get).toList();

        List<UserAuthority> userAuthoritiesToAdd = authorities.stream()
                .filter(filterOutEqualAuthorities(applicationUser))
                .map(authority -> new UserAuthority(applicationUser.getId(), authority.getName(), authority.getId()))
                .toList();

        return userAuthoritiesToAdd.stream()
                .map(userAuthorityRepository::save)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAuthority> deleteAuthorities(ApplicationUser applicationUser, AuthoritiesChange authoritiesChange) {
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

    @Override
    public List<UserAuthority> getByUserId(Long userId) {
        return userAuthorityRepository.findByUserId(userId);
    }

    @Override
    public List<SimpleGrantedAuthority> loadListAsSimpleGrantedAuthority(ApplicationUser applicationUser) {
        return userAuthorityRepository.findByUserId(applicationUser.getId())
                .stream()
                .map(userAuthorities -> new SimpleGrantedAuthority(userAuthorities.getName()))
                .collect(Collectors.toList());
    }

    private Predicate<Authority> filterOutEqualAuthorities(ApplicationUser applicationUser) {
        return authority -> applicationUser
                .getAuthorities()
                .stream()
                .noneMatch(userAuthority -> userAuthority.getName().equals(authority.getName())
                        && Objects.equals(userAuthority.getAuthorityId(), authority.getId()));
    }

    private Predicate<UserAuthority> filterUserRolesToRemove(List<Authority> authorities) {
        return userAuthority -> authorities.stream()
                .anyMatch(authority -> userAuthority.getName().equals(authority.getName())
                        && Objects.equals(userAuthority.getAuthorityId(), authority.getId())
                );
    }
}