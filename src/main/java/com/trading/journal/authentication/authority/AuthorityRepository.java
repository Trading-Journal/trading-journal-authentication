package com.trading.journal.authentication.authority;

import com.trading.journal.authentication.authority.service.impl.AuthorityServiceDatabaseImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@ConditionalOnBean(AuthorityServiceDatabaseImpl.class)
public interface AuthorityRepository extends CrudRepository<Authority, Long> {

    @Override
    List<Authority> findAll();

    List<Authority> getByCategory(AuthorityCategory category);

    Optional<Authority> getByName(String name);

}
