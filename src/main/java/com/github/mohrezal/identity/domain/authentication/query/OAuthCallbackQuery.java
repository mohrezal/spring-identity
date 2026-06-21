package com.github.mohrezal.identity.domain.authentication.query;

import com.github.mohrezal.identity.domain.authentication.dto.AuthResponse;
import com.github.mohrezal.identity.domain.authentication.dto.OAuthCallbackResponse;
import com.github.mohrezal.identity.domain.authentication.dto.oauth.OAuthStatePayload;
import com.github.mohrezal.identity.domain.authentication.dto.oauth.OAuthUserProfile;
import com.github.mohrezal.identity.domain.authentication.enums.OAuthFlowType;
import com.github.mohrezal.identity.domain.authentication.exception.type.OAuthEmailConflictException;
import com.github.mohrezal.identity.domain.authentication.listener.message.OAuthWelcomeEmailMessage;
import com.github.mohrezal.identity.domain.authentication.model.UserOauthConnection;
import com.github.mohrezal.identity.domain.authentication.query.param.OAuthCallbackQueryParams;
import com.github.mohrezal.identity.domain.authentication.repository.UserOauthConnectionRepository;
import com.github.mohrezal.identity.domain.authentication.service.TokenIssuanceService;
import com.github.mohrezal.identity.domain.authentication.service.oauth.OAuthLinkService;
import com.github.mohrezal.identity.domain.authentication.service.oauth.OAuthProviderRegistry;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import com.github.mohrezal.identity.shared.interfaces.Query;
import com.github.mohrezal.identity.shared.redis.RedisService;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthCallbackQuery implements Query<OAuthCallbackQueryParams, OAuthCallbackResponse> {

    private final RedisService redisService;
    private final OAuthProviderRegistry providerRegistry;
    private final UserRepository userRepository;
    private final UserOauthConnectionRepository userOauthConnectionRepository;
    private final TokenIssuanceService tokenIssuanceService;
    private final OAuthLinkService oAuthLinkService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void validate(OAuthCallbackQueryParams params) {
        if (params.provider() == null) {
            throw new UnauthorizedException();
        }

        if (params.code() == null || params.code().isBlank()) {
            throw new UnauthorizedException();
        }

        if (params.state() == null || params.state().isBlank()) {
            throw new UnauthorizedException();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OAuthCallbackResponse execute(OAuthCallbackQueryParams params) {
        validate(params);

        var payload =
                redisService
                        .consume(RedisKey.OAUTH_STATE, OAuthStatePayload.class, params.state())
                        .orElseThrow(UnauthorizedException::new);

        if (payload.redirectUrl() == null || payload.redirectUrl().isBlank()) {
            throw new UnauthorizedException();
        }

        if (payload.flowType() == null) {
            throw new UnauthorizedException();
        }

        if (!params.provider().equals(payload.provider())) {
            throw new UnauthorizedException();
        }

        if (payload.correlationId() == null
                || params.correlationId() == null
                || !payload.correlationId().equals(params.correlationId())) {
            throw new UnauthorizedException();
        }

        log.info(
                "OAuth callback started. provider={}, flowType={}",
                params.provider(),
                payload.flowType());

        var profile = providerRegistry.get(params.provider()).profile(params.code());
        validateProfile(params, profile);

        if (OAuthFlowType.LOGIN.equals(payload.flowType())) {
            var authResponse = login(profile, params.ipAddress(), params.userAgent());

            return new OAuthCallbackResponse(
                    authResponse, payload.redirectUrl(), payload.flowType());
        }

        if (OAuthFlowType.LINK.equals(payload.flowType())) {
            link(profile, payload);
            return new OAuthCallbackResponse(null, payload.redirectUrl(), payload.flowType());
        }

        throw new UnauthorizedException();
    }

    private void validateProfile(OAuthCallbackQueryParams params, OAuthUserProfile profile) {
        if (profile == null
                || profile.provider() == null
                || !params.provider().equals(profile.provider())
                || profile.providerUserId() == null
                || profile.providerUserId().isBlank()
                || profile.email() == null
                || profile.email().isBlank()
                || !profile.emailVerified()) {
            throw new UnauthorizedException();
        }
    }

    private AuthResponse login(OAuthUserProfile profile, String ipAddress, String deviceInfo) {
        var connection =
                userOauthConnectionRepository.findByProviderAndProviderUserId(
                        profile.provider(), profile.providerUserId());

        if (connection.isPresent()) {
            var user = connection.get().getUser();
            log.info(
                    "OAuth login matched existing connection. provider={}, userId={}",
                    profile.provider(),
                    user.getId());
            return tokenIssuanceService.issue(user, ipAddress, deviceInfo);
        }

        if (userRepository.existsUserByEmail(profile.email())) {
            log.warn("OAuth login blocked by email conflict. provider={}", profile.provider());
            throw new OAuthEmailConflictException();
        }

        var user =
                User.builder()
                        .email(profile.email())
                        .firstName(profile.firstName())
                        .lastName(profile.lastName())
                        .emailVerifiedAt(OffsetDateTime.now())
                        .build();

        var savedUser = userRepository.save(user);
        log.info(
                "OAuth login created user. provider={}, userId={}",
                profile.provider(),
                savedUser.getId());

        var oauthConnection =
                UserOauthConnection.builder()
                        .user(savedUser)
                        .provider(profile.provider())
                        .providerUserId(profile.providerUserId())
                        .email(profile.email())
                        .build();

        userOauthConnectionRepository.save(oauthConnection);
        log.info(
                "OAuth connection created during login. provider={}, userId={}",
                profile.provider(),
                savedUser.getId());

        eventPublisher.publishEvent(
                new OAuthWelcomeEmailMessage(
                        savedUser.getId(), savedUser.getEmail(), profile.provider()));

        return tokenIssuanceService.issue(savedUser, ipAddress, deviceInfo);
    }

    private void link(OAuthUserProfile profile, OAuthStatePayload payload) {
        if (payload.userId() == null) {
            throw new UnauthorizedException();
        }

        oAuthLinkService.link(
                payload.userId(), profile.provider(), profile.providerUserId(), profile.email());
    }
}
