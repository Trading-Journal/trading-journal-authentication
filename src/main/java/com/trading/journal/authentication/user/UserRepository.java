package com.trading.journal.authentication.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Boolean existsByUserName(String userName);

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT COUNT(Users.id) FROM Users inner join UserAuthorities on Users.id = UserAuthorities.userId inner join Authorities on Authorities.id = UserAuthorities.authorityId where Authorities.name in  (:roles)", nativeQuery = true)
    Integer countAdmins(List<String> roles);

    List<User> findByTenancyId(Long tenancyId);
}