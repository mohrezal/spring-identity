package com.github.mohrezal.identity.domain.authentication.repository;

import com.github.mohrezal.identity.domain.authentication.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.authentication.model.UserOauthConnection;
import com.github.mohrezal.identity.domain.user.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOauthConnectionRepository extends JpaRepository<UserOauthConnection, UUID> {
    Optional<UserOauthConnection> findByProviderAndProviderUserId(
            OAuthProviderType provider, String providerUserId);

    boolean existsByProviderAndProviderUserId(OAuthProviderType provider, String providerUserId);

    boolean existsByEmailAndUser_IdNot(String email, UUID userId);

    List<UserOauthConnection> findAllByUser(User user);

    Optional<UserOauthConnection> findByIdAndUser(UUID id, User user);

    long countByUser(User user);
}
