package com.trading.journal.authentication.authority;

import reactor.core.publisher.Flux;

public interface AuthorityService {

    Flux<Authority> getAuthoritiesByCategory(AuthorityCategory category);
}
