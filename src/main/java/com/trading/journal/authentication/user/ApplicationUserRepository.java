package com.trading.journal.authentication.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationUserRepository extends PagingAndSortingRepository<ApplicationUser, Long>, JpaSpecificationExecutor<ApplicationUser> {

    Boolean existsByUserName(String userName);

    Boolean existsByEmail(String email);

    Optional<ApplicationUser> findByEmail(String email);

    @Query(value = "SELECT COUNT(Users.id) FROM Users inner join UserAuthorities where Users.id = UserAuthorities.userId and UserAuthorities.name in (:roles)", nativeQuery = true)
    Integer countAdmins(List<String> roles);
}