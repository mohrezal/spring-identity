package com.github.mohrezal.identity.domain.privilege.repository;

import com.github.mohrezal.identity.domain.privilege.model.Role;
import com.github.mohrezal.identity.domain.privilege.model.UserRole;
import com.github.mohrezal.identity.domain.user.model.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findAllByUser(User user);

    @EntityGraph(attributePaths = {"role.permissions", "role.permissions.permission"})
    List<UserRole> findAllByUser_Id(UUID userId);

    List<UserRole> findAllByRole(Role role);

    boolean existsByUserAndRole(User user, Role role);

    boolean existsByRole(Role role);

    @Query(
            """
            SELECT DISTINCT permission.key
            FROM UserRole userRole
            JOIN userRole.role role
            JOIN role.permissions rolePermission
            JOIN rolePermission.permission permission
            WHERE userRole.user.id = :userId
              AND role.enabled = true
              AND permission.enabled = true
            """)
    List<String> findPermissionKeysByUserId(@Param("userId") UUID userId);
}
