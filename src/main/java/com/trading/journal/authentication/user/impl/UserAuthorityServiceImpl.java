package com.trading.journal.authentication.user.impl;

import com.trading.journal.authentication.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAuthorityServiceImpl implements UserAuthorityService {

    private final UserAuthorityRepository userAuthorityRepository;

    @Override
    public Mono<UserAuthority> saveBasicUserAuthorities(ApplicationUser applicationUser) {
        return Flux.just(new UserAuthority(applicationUser.getId(), AuthoritiesHelper.ROLE_USER))
                .flatMap(userAuthorityRepository::save)
                .last();
    }

    @Override
    public Mono<List<UserAuthority>> loadListUserAuthority(ApplicationUser applicationUser) {
        return userAuthorityRepository.findByUserId(applicationUser.getId()).collectList();
    }

    @Override
    public Mono<List<UserAuthority>> loadListUserAuthority(Long userId) {
        return userAuthorityRepository.findByUserId(userId).collectList();
    }

    @Override
    public Mono<List<SimpleGrantedAuthority>> loadListOfSimpleGrantedAuthority(ApplicationUser applicationUser) {
        return userAuthorityRepository.findByUserId(applicationUser.getId())
                .map(userAuthorities -> new SimpleGrantedAuthority(userAuthorities.getName()))
                .collectList();
    }
}
