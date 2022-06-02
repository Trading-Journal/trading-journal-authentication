package com.trading.journal.authentication.authority.service.impl;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.service.AuthorityRepository;
import com.trading.journal.authentication.authority.service.AuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "journal.authentication.authority", name = "type", havingValue = "DATABASE")
public class AuthorityServiceDatabaseImpl implements AuthorityService {

    private final AuthorityRepository authorityRepository;

    @Override
    public Flux<Authority> getAuthoritiesByCategory(AuthorityCategory category) {
        return authorityRepository.getByCategory(category);
    }
}
