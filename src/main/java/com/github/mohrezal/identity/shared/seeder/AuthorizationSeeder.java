package com.github.mohrezal.identity.shared.seeder;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.domain.authorization.constant.Permissions;
import com.github.mohrezal.identity.domain.authorization.model.Role;
import com.github.mohrezal.identity.domain.authorization.model.RolePermission;
import com.github.mohrezal.identity.domain.authorization.model.UserRole;
import com.github.mohrezal.identity.domain.authorization.repository.RolePermissionRepository;
import com.github.mohrezal.identity.domain.authorization.repository.RoleRepository;
import com.github.mohrezal.identity.domain.authorization.repository.UserRoleRepository;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@Profile("seed")
@RequiredArgsConstructor
public class AuthorizationSeeder implements CommandLineRunner {

    private final ApplicationProperties applicationProperties;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final PlatformTransactionManager transactionManager;
    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void run(String... args) {
        var transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(
                status -> {
                    var ownerRole = seedOwnerRole();
                    seedOwnerPermissions(ownerRole);
                    assignOwnerRole(ownerRole);
                    return null;
                });

        log.info("Authorization seeding completed.");
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }

    private Role seedOwnerRole() {
        var owner = applicationProperties.seed().owner();
        return roleRepository
                .findByKey(owner.roleKey())
                .orElseGet(
                        () ->
                                roleRepository.save(
                                        Role.builder()
                                                .key(owner.roleKey())
                                                .name(owner.roleName())
                                                .build()));
    }

    private void seedOwnerPermissions(Role ownerRole) {
        var rolePermissions =
                Permissions.ALL.stream()
                        .filter(
                                permissionKey ->
                                        !rolePermissionRepository.existsByRoleAndPermissionKey(
                                                ownerRole, permissionKey))
                        .map(
                                permissionKey ->
                                        RolePermission.builder()
                                                .role(ownerRole)
                                                .permissionKey(permissionKey)
                                                .build())
                        .toList();

        rolePermissionRepository.saveAll(rolePermissions);
    }

    private void assignOwnerRole(Role ownerRole) {
        var ownerEmail = applicationProperties.seed().owner().email();
        if (!StringUtils.hasText(ownerEmail)) {
            log.info("No seed owner email configured. Skipping owner role assignment.");
            return;
        }

        var owner =
                userRepository
                        .findByEmail(ownerEmail)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Owner user not found: " + ownerEmail));

        if (userRoleRepository.existsByUserAndRole(owner, ownerRole)) {
            return;
        }

        var userRole = UserRole.builder().user(owner).role(ownerRole).build();
        userRoleRepository.save(userRole);
    }
}
