package com.trading.journal.authentication.authority.service.impl;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.AuthorityRepository;
import com.trading.journal.authentication.authority.service.AuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityRepository authorityRepository;

    @Override
    public List<Authority> getAuthoritiesByCategory(AuthorityCategory category) {
        return authorityRepository.getByCategory(category);
    }

    @Override
    public List<Authority> getAll() {
        return authorityRepository.findAll();
    }

    @Override
    public Optional<Authority> getByName(String name) {
        return authorityRepository.getByName(name);
    }
}
