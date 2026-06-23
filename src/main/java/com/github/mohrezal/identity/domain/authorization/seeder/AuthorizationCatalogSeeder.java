package com.github.mohrezal.identity.domain.authorization.seeder;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.domain.authorization.constant.PermissionCatalog;
import com.github.mohrezal.identity.domain.authorization.constant.PermissionCatalog.Definition;
import com.github.mohrezal.identity.domain.authorization.model.Permission;
import com.github.mohrezal.identity.domain.authorization.model.Role;
import com.github.mohrezal.identity.domain.authorization.model.RolePermission;
import com.github.mohrezal.identity.domain.authorization.repository.PermissionRepository;
import com.github.mohrezal.identity.domain.authorization.repository.RolePermissionRepository;
import com.github.mohrezal.identity.domain.authorization.repository.RoleRepository;
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
@Profile("seed-authorization")
@RequiredArgsConstructor
public class AuthorizationCatalogSeeder implements CommandLineRunner {

    private final ApplicationProperties applicationProperties;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final ConfigurableApplicationContext applicationContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(String... args) {
        var roles = applicationProperties.authorization().role();

        seedRole(roles.owner(), PermissionCatalog.ALL);
        seedRole(roles.user(), PermissionCatalog.USER);

        log.info("Authorization catalog seeding completed.");
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }

    private Role seedRole(
            ApplicationProperties.Authorization.Properties properties,
            List<Definition> permissions) {
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
        return permissionRepository
                .findByKey(definition.key())
                .map(
                        permission -> {
                            permission.setName(definition.name());
                            permission.setService(definition.service());
                            return permission;
                        })
                .orElseGet(
                        () ->
                                permissionRepository.save(
                                        Permission.builder()
                                                .key(definition.key())
                                                .name(definition.name())
                                                .service(definition.service())
                                                .build()));
    }
}
