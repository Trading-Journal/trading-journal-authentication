package com.trading.journal.authentication.authority.service.impl;

import com.trading.journal.authentication.authority.*;
import com.trading.journal.authentication.authority.service.AuthorityService;
import com.trading.journal.authentication.authority.service.UserAuthorityRepository;
import com.trading.journal.authentication.authority.service.UserAuthorityService;
import com.trading.journal.authentication.user.ApplicationUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAuthorityServiceImpl implements UserAuthorityService {

    private final UserAuthorityRepository userAuthorityRepository;

    private final AuthorityService authorityService;

    @Override
    public Mono<UserAuthority> saveCommonUserAuthorities(ApplicationUser applicationUser) {
        return authorityService.getAuthoritiesByCategory(AuthorityCategory.COMMON_USER)
                .map(authority -> new UserAuthority(applicationUser.getId(), authority.getName(), authority.getId()))
                .flatMap(userAuthorityRepository::save)
                .last();
    }

    @Override
    public Mono<List<UserAuthority>> loadList(ApplicationUser applicationUser) {
        return userAuthorityRepository.findByUserId(applicationUser.getId()).collectList();
    }

    @Override
    public Mono<List<UserAuthority>> loadList(Long userId) {
        return userAuthorityRepository.findByUserId(userId).collectList();
    }

    @Override
    public Mono<List<SimpleGrantedAuthority>> loadListAsSimpleGrantedAuthority(ApplicationUser applicationUser) {
        return userAuthorityRepository.findByUserId(applicationUser.getId())
                .map(userAuthorities -> new SimpleGrantedAuthority(userAuthorities.getName()))
                .collectList();
    }
}
