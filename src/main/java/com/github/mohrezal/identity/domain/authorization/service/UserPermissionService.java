package com.github.mohrezal.identity.domain.authorization.service;

import com.github.mohrezal.identity.domain.authorization.repository.UserRoleRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPermissionService {

    private final UserRoleRepository userRoleRepository;

    @Transactional(readOnly = true)
    public List<String> getPermissionKeys(UUID userId) {
        return userRoleRepository.findPermissionKeysByUserId(userId);
    }
}
