package com.github.mohrezal.identity.domain.user.repository;

import com.github.mohrezal.identity.domain.user.model.UserCredential;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {}
