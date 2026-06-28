package com.github.mohrezal.identity.domain.privilege.repository;

import com.github.mohrezal.identity.domain.privilege.model.Role;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByKey(String key);

    boolean existsByKey(String key);

    @EntityGraph(attributePaths = {"permissions", "permissions.permission"})
    @Query("SELECT role FROM Role role")
    List<Role> findAllWithPermissions();

    @EntityGraph(attributePaths = {"permissions", "permissions.permission"})
    @Query("SELECT role FROM Role role WHERE role.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"permissions", "permissions.permission"})
    @Query("SELECT role FROM Role role WHERE role.id IN :ids")
    List<Role> findAllByIdWithPermissions(@Param("ids") Set<UUID> ids);
}
