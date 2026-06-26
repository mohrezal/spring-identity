package com.github.mohrezal.identity.domain.privilege.seeder;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.domain.privilege.model.Role;
import com.github.mohrezal.identity.domain.privilege.repository.RoleRepository;
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
@Profile("seed-role")
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final ApplicationProperties applicationProperties;
    private final RoleRepository roleRepository;
    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void run(String... args) {
        seedRoles();
        log.info("Role seeding completed.");
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }

    @Transactional(rollbackFor = Exception.class)
    public void seedRoles() {
        var roles = applicationProperties.privilege().role();
        seedRole(roles.owner());
        seedRole(roles.user());
    }

    private void seedRole(ApplicationProperties.Privilege.Properties properties) {
        roleRepository
                .findByKey(properties.key())
                .orElseGet(
                        () -> {
                            log.info(
                                    "Creating role: key={}, name={}",
                                    properties.key(),
                                    properties.name());
                            return roleRepository.save(
                                    Role.builder()
                                            .key(properties.key())
                                            .name(properties.name())
                                            .build());
                        });
    }
}
