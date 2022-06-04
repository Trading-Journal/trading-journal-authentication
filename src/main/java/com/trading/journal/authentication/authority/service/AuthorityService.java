package com.trading.journal.authentication.authority.service;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import reactor.core.publisher.Flux;

public interface AuthorityService {

    Flux<Authority> getAuthoritiesByCategory(AuthorityCategory category);

    Flux<Authority> getAll();
}
