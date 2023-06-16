package com.trading.journal.authentication.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserManagementRepository extends CrudRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByTenancyIdAndId(Long tenancyId, Long id);

    Optional<User> findByTenancyIdAndEmail(Long tenancyId, String email);

    Boolean existsByTenancyIdAndEmailAndIdNot(Long tenancyId, String email, Long id);
}