package com.trading.journal.authentication.user;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface UserAuthorityRepository  extends ReactiveCrudRepository<UserAuthority, Long> {

    Flux<UserAuthority> findByUserId(Long userId);
}
