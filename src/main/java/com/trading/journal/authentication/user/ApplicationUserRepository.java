package com.trading.journal.authentication.user;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApplicationUserRepository extends CrudRepository<ApplicationUser, Long> {

    Boolean existsByUserName(String userName);

    Boolean existsByEmail(String email);

    ApplicationUser findByEmail(String email);

    @Query("select id, userName, firstName, lastName, email, enabled, verified, createdAt from Users where email = :email")
    UserInfo getUserInfoByEmail(String email);

    @Query("SELECT COUNT(Users.id) FROM Users inner join UserAuthorities where Users.id = UserAuthorities.userId and UserAuthorities.name in (:roles)")
    Integer countAdmins(List<String> roles);
}