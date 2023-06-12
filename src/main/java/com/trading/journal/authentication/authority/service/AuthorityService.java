package com.trading.journal.authentication.authority.service;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;

import java.util.List;
import java.util.Optional;

public interface AuthorityService {

    List<Authority> getAuthoritiesByCategory(AuthorityCategory category);

    List<Authority> getAll();

    Optional<Authority> getByName(String name);

    List<AuthorityCategory> getAllCategories();

    Authority getById(Long id);

    Authority add(Authority authority);

    Authority update(Long id, Authority authority);

    void delete(Long id);
}
