package com.trading.journal.authentication.authority.service;

import com.trading.journal.authentication.authority.UserAuthority;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserAuthorityRepository extends CrudRepository<UserAuthority, Long> {

    @Override
    List<UserAuthority> findAll();

    List<UserAuthority> findByUserId(Long userId);
}
