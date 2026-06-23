package com.github.mohrezal.identity.domain.privilege.repository;

import com.github.mohrezal.identity.domain.privilege.model.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByKey(String key);

    boolean existsByKey(String key);
}
