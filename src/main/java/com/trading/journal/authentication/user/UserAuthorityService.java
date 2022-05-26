package com.trading.journal.authentication.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserAuthorityService {

    Mono<UserAuthority> saveBasicUserAuthorities(ApplicationUser applicationUser);

    Mono<List<UserAuthority>> loadListUserAuthority(ApplicationUser applicationUser);

    Mono<List<UserAuthority>> loadListUserAuthority(Long userId);

    Mono<List<SimpleGrantedAuthority>> loadListOfSimpleGrantedAuthority(ApplicationUser applicationUser);
}
