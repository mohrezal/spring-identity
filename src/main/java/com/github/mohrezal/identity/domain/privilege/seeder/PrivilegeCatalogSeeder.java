package com.github.mohrezal.identity.domain.privilege.seeder;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.domain.privilege.constant.PermissionCatalog;
import com.github.mohrezal.identity.domain.privilege.constant.PermissionCatalog.Definition;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.domain.privilege.model.Permission;
import com.github.mohrezal.identity.domain.privilege.model.Role;
import com.github.mohrezal.identity.domain.privilege.model.RolePermission;
import com.github.mohrezal.identity.domain.privilege.repository.PermissionRepository;
import com.github.mohrezal.identity.domain.privilege.repository.RolePermissionRepository;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Order(1)
@Component
@Profile("seed-privilege")
@RequiredArgsConstructor
public class PrivilegeCatalogSeeder implements CommandLineRunner {

    private final ApplicationProperties applicationProperties;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void run(String... args) {
        seedPrivilegeCatalog();
        log.info("Privilege catalog seeding completed.");
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }

    @Transactional(rollbackFor = Exception.class)
    public void seedPrivilegeCatalog() {
        var roles = applicationProperties.privilege().role();
        seedRole(roles.owner(), PermissionCatalog.ALL);
        seedRole(roles.user(), PermissionCatalog.USER);
    }

    private Role seedRole(
            ApplicationProperties.Privilege.Properties properties, List<Definition> permissions) {
        var role =
                roleRepository
                        .findByKey(properties.key())
                        .orElseGet(
                                () ->
                                        roleRepository.save(
                                                Role.builder()
                                                        .key(properties.key())
                                                        .name(properties.name())
                                                        .build()));

        seedPermissions(role, permissions);
        return role;
    }

    private void seedPermissions(Role role, List<Definition> permissionDefinitions) {
        var rolePermissions =
                permissionDefinitions.stream()
                        .map(this::seedPermission)
                        .filter(
                                permission ->
                                        !rolePermissionRepository.existsByRoleAndPermission(
                                                role, permission))
                        .map(
                                permission ->
                                        RolePermission.builder()
                                                .role(role)
                                                .permission(permission)
                                                .build())
                        .toList();

        rolePermissionRepository.saveAll(rolePermissions);
    }

    private Permission seedPermission(Definition definition) {
        var permission =
                permissionRepository
                        .findByKey(definition.key())
                        .orElseGet(
                                () ->
                                        permissionRepository.save(
                                                Permission.builder()
                                                        .key(definition.key())
                                                        .name(definition.name())
                                                        .service(definition.service())
                                                        .build()));

        if (Permissions.IDENTITY_PRIVILEGE_PERMISSIONS_UPDATE.equals(permission.getKey())
                && Boolean.FALSE.equals(permission.getEnabled())) {
            permission.setEnabled(true);
            log.warn("Re-enabling protected permission. key={}", permission.getKey());
            return permissionRepository.save(permission);
        }

        return permission;
    }
}
