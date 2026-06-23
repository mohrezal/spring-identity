package com.github.mohrezal.identity.domain.auth.service.oauth;

import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthEmailConflictException;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthEmailMismatchException;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthProviderAlreadyLinkedException;
import com.github.mohrezal.identity.domain.auth.listener.message.OAuthLinkEmailMessage;
import com.github.mohrezal.identity.domain.auth.model.UserOauthConnection;
import com.github.mohrezal.identity.domain.auth.repository.UserOauthConnectionRepository;
import com.github.mohrezal.identity.domain.user.exception.type.UserNotFoundException;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthLinkService {
    private final UserRepository userRepository;
    private final UserOauthConnectionRepository userOauthConnectionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public User validate(
            UUID userId, OAuthProviderType provider, String providerUserId, String email) {
        if (userOauthConnectionRepository.existsByProviderAndProviderUserId(
                provider, providerUserId)) {
            log.warn(
                    "OAuth link blocked because provider account is already linked. provider={},"
                            + " userId={}",
                    provider,
                    userId);
            throw new OAuthProviderAlreadyLinkedException();
        }

        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (!user.getEmail().equalsIgnoreCase(email)) {
            log.warn(
                    "OAuth link blocked by provider email mismatch. provider={}, userId={}",
                    provider,
                    user.getId());
            throw new OAuthEmailMismatchException();
        }

        userRepository
                .findByEmail(email)
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(
                        existingUser -> {
                            log.warn(
                                    "OAuth link blocked by user email conflict. provider={},"
                                            + " userId={}, conflictingUserId={}",
                                    provider,
                                    user.getId(),
                                    existingUser.getId());
                            throw new OAuthEmailConflictException();
                        });

        if (userOauthConnectionRepository.existsByEmailAndUser_IdNot(email, user.getId())) {
            log.warn(
                    "OAuth link blocked by existing connection email conflict. provider={},"
                            + " userId={}",
                    provider,
                    user.getId());
            throw new OAuthEmailConflictException();
        }

        return user;
    }

    public void link(UUID userId, OAuthProviderType provider, String providerUserId, String email) {
        var user = validate(userId, provider, providerUserId, email);
        var oauthConnection =
                UserOauthConnection.builder()
                        .user(user)
                        .provider(provider)
                        .providerUserId(providerUserId)
                        .email(email)
                        .build();

        userOauthConnectionRepository.save(oauthConnection);
        log.info("OAuth connection linked. provider={}, userId={}", provider, user.getId());

        eventPublisher.publishEvent(
                new OAuthLinkEmailMessage(user.getId(), user.getEmail(), provider));
    }
}
