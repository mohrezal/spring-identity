package com.github.mohrezal.identity.domain.authorization.repository;

import com.github.mohrezal.identity.domain.authorization.enums.Permission;
import com.github.mohrezal.identity.domain.authorization.model.Role;
import com.github.mohrezal.identity.domain.authorization.model.UserRole;
import com.github.mohrezal.identity.domain.user.model.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findAllByUser(User user);

    List<UserRole> findAllByRole(Role role);

    boolean existsByUserAndRole(User user, Role role);

    @Query(
            """
            SELECT DISTINCT rolePermission.permission
            FROM UserRole userRole
            JOIN userRole.role role
            JOIN role.permissions rolePermission
            WHERE userRole.user.id = :userId
              AND role.enabled = true
            """)
    List<Permission> findPermissionsByUserId(@Param("userId") UUID userId);
}
