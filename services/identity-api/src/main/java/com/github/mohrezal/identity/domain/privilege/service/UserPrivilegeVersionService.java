package com.github.mohrezal.identity.domain.privilege.service;

import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPrivilegeVersionService {

    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    public void increment(User user) {
        user.incrementPrivilegeVersion();
        userRepository.save(user);
        log.info(
                "Incremented privilege version. userId={}, privilegeVersion={}",
                user.getId(),
                user.getPrivilegeVersion());
    }
}
