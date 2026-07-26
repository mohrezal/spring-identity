package com.github.mohrezal.identity.domain.privilege.seeder;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.domain.privilege.model.UserRole;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
import com.github.mohrezal.identity.domain.privilege.repository.UserRoleRepository;
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
    public void run(String... args) {
        assignOwnerRole();
        log.info("Owner role assignment completed.");
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignOwnerRole() {
        var roleKey = applicationProperties.privilege().role().owner().key();
        var role =
                roleRepository
                        .findByKey(roleKey)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Owner role not found: " + roleKey));

        var ownerEmail = applicationProperties.owner().email();
        var owner =
                userRepository
                        .findByEmail(ownerEmail)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Owner user not found: " + ownerEmail));

        if (!userRoleRepository.existsByUserAndRole(owner, role)) {
            userRoleRepository.save(UserRole.builder().user(owner).role(role).build());
        }
    }
}
