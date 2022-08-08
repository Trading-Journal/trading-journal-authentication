package com.trading.journal.authentication.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface UserManagementRepository extends Repository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByTenancyIdAndId(Long tenancyId, Long id);

    User save(User user);

    void delete(User user);

    Optional<User> findByTenancyIdAndEmail(Long tenancyId, String email);

    Boolean existsByTenancyIdAndUserNameAndIdNot(Long tenancyId, String userName, Long id);
}