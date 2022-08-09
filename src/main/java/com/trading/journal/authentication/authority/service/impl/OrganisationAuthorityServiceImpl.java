package com.trading.journal.authentication.authority.service.impl;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.AuthorityRepository;
import com.trading.journal.authentication.authority.service.OrganisationAuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganisationAuthorityServiceImpl implements OrganisationAuthorityService {

    private final AuthorityRepository authorityRepository;

    @Override
    public List<Authority> getAllNonAdmin() {
        return authorityRepository.getByCategoryNot(AuthorityCategory.ADMINISTRATOR);
    }
}
