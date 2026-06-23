package com.github.mohrezal.identity.domain.authorization.seeder;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.domain.authorization.model.Role;
import com.github.mohrezal.identity.domain.authorization.model.UserRole;
import com.github.mohrezal.identity.domain.authorization.repository.RoleRepository;
import com.github.mohrezal.identity.domain.authorization.repository.UserRoleRepository;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
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
@Order(2)
@Component
@Profile("seed-owner")
@RequiredArgsConstructor
public class OwnerRoleSeeder implements CommandLineRunner {

    private final ApplicationProperties applicationProperties;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final ConfigurableApplicationContext applicationContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(String... args) {
        assignOwnerRole(getOwnerRole());
        log.info("Owner role seeding completed.");
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }

    private Role getOwnerRole() {
        var roleKey = applicationProperties.authorization().role().owner().key();
        return roleRepository
                .findByKey(roleKey)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Configured owner role not found: " + roleKey));
    }

    private void assignOwnerRole(Role ownerRole) {
        var ownerEmail = applicationProperties.owner().email();
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

        userRoleRepository.save(UserRole.builder().user(owner).role(ownerRole).build());
    }
}
