package com.trading.journal.authentication.userauthority;

import com.trading.journal.authentication.user.ApplicationUser;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserAuthorityRepository extends CrudRepository<UserAuthority, Long> {

    @Override
    List<UserAuthority> findAll();

    List<UserAuthority> findByApplicationUser(ApplicationUser applicationUser);
}
