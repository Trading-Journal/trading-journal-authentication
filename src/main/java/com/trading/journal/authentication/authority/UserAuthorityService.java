package com.trading.journal.authentication.authority;

import com.trading.journal.authentication.user.ApplicationUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserAuthorityService {

    Mono<UserAuthority> saveCommonUserAuthorities(ApplicationUser applicationUser);

    Mono<List<UserAuthority>> loadList(ApplicationUser applicationUser);

    Mono<List<UserAuthority>> loadList(Long userId);

    Mono<List<SimpleGrantedAuthority>> loadListAsSimpleGrantedAuthority(ApplicationUser applicationUser);
}
