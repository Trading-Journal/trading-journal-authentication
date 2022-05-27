package com.trading.journal.authentication.authority;

import com.trading.journal.authentication.authority.impl.AuthorityServiceDatabaseImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ConditionalOnBean(AuthorityServiceDatabaseImpl.class)
public interface AuthorityRepository extends ReactiveCrudRepository<Authority, Long> {

    Flux<Authority> getByCategory(AuthorityCategory category);

    Mono<Authority> getByName(String name);
}
