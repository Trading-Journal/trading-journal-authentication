package com.trading.journal.authentication.authority.service.impl;

import com.trading.journal.authentication.authority.AuthoritiesHelper;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.service.AuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "journal.authentication.authority", name = "type", havingValue = "STATIC", matchIfMissing = true)
public class AuthorityServiceStaticImpl implements AuthorityService {

    @Override
    public Flux<Authority> getAuthoritiesByCategory(AuthorityCategory category) {
        Stream<Authority> authorities = AuthoritiesHelper.getByCategory(category).stream().map(authoritiesHelper -> new Authority(authoritiesHelper.getCategory(), authoritiesHelper.getLabel()));
        return Flux.fromStream(authorities);
    }

    @Override
    public Flux<Authority> getAll() {
        Stream<Authority> authorities = Arrays.stream(AuthoritiesHelper.values()).map(authoritiesHelper -> new Authority(authoritiesHelper.getCategory(), authoritiesHelper.getLabel()));
        return Flux.fromStream(authorities);
    }
}
