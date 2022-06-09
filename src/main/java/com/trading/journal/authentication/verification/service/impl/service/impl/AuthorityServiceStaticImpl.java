package com.trading.journal.authentication.verification.service.impl.service.impl;

import com.trading.journal.authentication.authority.AuthoritiesHelper;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.verification.service.impl.service.AuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "journal.authentication.authority", name = "type", havingValue = "STATIC", matchIfMissing = true)
public class AuthorityServiceStaticImpl implements AuthorityService {

    @Override
    public List<Authority> getAuthoritiesByCategory(AuthorityCategory category) {
        return AuthoritiesHelper.getByCategory(category)
                .stream()
                .map(authoritiesHelper -> new Authority(authoritiesHelper.getCategory(), authoritiesHelper.getLabel()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Authority> getAll() {
        return Arrays.stream(AuthoritiesHelper.values())
                .map(authoritiesHelper -> new Authority(authoritiesHelper.getCategory(), authoritiesHelper.getLabel()))
                .collect(Collectors.toList());
    }
}
