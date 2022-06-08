package com.trading.journal.authentication.verification.service.impl.service;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;

import java.util.List;

public interface AuthorityService {

    List<Authority> getAuthoritiesByCategory(AuthorityCategory category);

    List<Authority> getAll();
}
