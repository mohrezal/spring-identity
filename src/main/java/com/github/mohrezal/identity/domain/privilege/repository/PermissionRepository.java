package com.github.mohrezal.identity.domain.privilege.repository;

import com.github.mohrezal.identity.domain.privilege.model.Permission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByKey(String key);

    boolean existsByKey(String key);
}
