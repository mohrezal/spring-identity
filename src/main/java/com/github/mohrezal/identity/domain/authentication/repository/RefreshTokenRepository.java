package com.github.mohrezal.identity.domain.authentication.repository;

import com.github.mohrezal.identity.domain.authentication.model.RefreshToken;
import com.github.mohrezal.identity.domain.user.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByHashedToken(String hashedToken);

    List<RefreshToken> findAllByUserAndRevokedAtIsNull(User user);

    Optional<RefreshToken> findByIdAndUser(UUID id, User user);
}
