package com.trading.journal.authentication.authority.service;

import com.trading.journal.authentication.authority.Authority;

import java.util.List;

public interface OrganisationAuthorityService {
    List<Authority> getAllNonAdmin();
}
