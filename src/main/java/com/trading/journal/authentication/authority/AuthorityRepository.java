package com.trading.journal.authentication.authority;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

@ConditionalOnProperty(prefix = "journal.authentication.authority", name = "type", havingValue = "DATABASE")
public interface AuthorityRepository extends ReactiveCrudRepository<Authority, Long> {
}
