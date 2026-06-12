package com.github.mohrezal.identity.domain.auth.repository;

import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.auth.model.UserOauthConnection;
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
}
