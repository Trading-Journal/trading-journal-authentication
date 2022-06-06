package com.trading.journal.authentication.authority.service;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.service.impl.AuthorityServiceDatabaseImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@ConditionalOnBean(AuthorityServiceDatabaseImpl.class)
public interface AuthorityRepository extends CrudRepository<Authority, Long> {

    @Override
    List<Authority> findAll();

    List<Authority> getByCategory(AuthorityCategory category);

    Authority getByName(String name);

}
