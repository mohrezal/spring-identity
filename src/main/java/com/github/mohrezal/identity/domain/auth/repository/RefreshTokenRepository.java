package com.github.mohrezal.identity.domain.auth.repository;

import com.github.mohrezal.identity.domain.auth.model.RefreshToken;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {}
