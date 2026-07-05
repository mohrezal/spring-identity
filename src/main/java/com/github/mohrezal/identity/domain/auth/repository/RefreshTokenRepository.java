package com.github.mohrezal.identity.domain.auth.repository;

import com.github.mohrezal.identity.domain.auth.model.RefreshToken;
import com.github.mohrezal.identity.domain.user.model.User;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByHashedToken(String hashedToken);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT token FROM RefreshToken token WHERE token.hashedToken = :hashedToken")
    Optional<RefreshToken> findByHashedTokenForUpdate(@Param("hashedToken") String hashedToken);

    List<RefreshToken> findAllByUserAndRevokedAtIsNull(User user);

    Optional<RefreshToken> findByIdAndUser(UUID id, User user);
}
