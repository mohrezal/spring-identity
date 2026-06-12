package com.github.mohrezal.identity.domain.auth.query;

import com.github.mohrezal.identity.domain.auth.dto.AuthResponse;
import com.github.mohrezal.identity.domain.auth.dto.OAuthCallbackResponse;
import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthStatePayload;
import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthUserProfile;
import com.github.mohrezal.identity.domain.auth.enums.OAuthErrorCode;
import com.github.mohrezal.identity.domain.auth.enums.OAuthFlowType;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthCallbackRedirectException;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthEmailConflictException;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthProviderAlreadyLinkedException;
import com.github.mohrezal.identity.domain.auth.model.UserOauthConnection;
import com.github.mohrezal.identity.domain.auth.query.param.OAuthCallbackQueryParams;
import com.github.mohrezal.identity.domain.auth.repository.UserOauthConnectionRepository;
import com.github.mohrezal.identity.domain.auth.service.TokenIssuanceService;
import com.github.mohrezal.identity.domain.auth.service.oauth.OAuthProviderRegistry;
import com.github.mohrezal.identity.domain.user.exception.type.UserNotFoundException;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import com.github.mohrezal.identity.shared.interfaces.Query;
import com.github.mohrezal.identity.shared.redis.RedisService;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        try {
            log.info(
                    "OAuth callback started. provider={}, flowType={}",
                    params.provider(),
                    payload.flowType());

            var profile = providerRegistry.get(params.provider()).profile(params.code());

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
        } catch (Exception exception) {
            log.warn(
                    "OAuth callback failed. provider={}, flowType={}, redirectUrl={}",
                    params.provider(),
                    payload.flowType(),
                    payload.redirectUrl(),
                    exception);

            throw new OAuthCallbackRedirectException(
                    payload.redirectUrl(), OAuthErrorCode.from(exception), exception);
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

        return tokenIssuanceService.issue(savedUser, ipAddress, deviceInfo);
    }

    private void link(OAuthUserProfile profile, OAuthStatePayload payload) {
        if (payload.userId() == null) {
            throw new UnauthorizedException();
        }

        if (userOauthConnectionRepository.existsByProviderAndProviderUserId(
                profile.provider(), profile.providerUserId())) {
            log.warn(
                    "OAuth link blocked because provider account is already linked. provider={},"
                            + " userId={}",
                    profile.provider(),
                    payload.userId());
            throw new OAuthProviderAlreadyLinkedException();
        }

        var user =
                userRepository.findById(payload.userId()).orElseThrow(UserNotFoundException::new);

        userRepository
                .findByEmail(profile.email())
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(
                        existingUser -> {
                            log.warn(
                                    "OAuth link blocked by user email conflict. provider={},"
                                            + " userId={}, conflictingUserId={}",
                                    profile.provider(),
                                    user.getId(),
                                    existingUser.getId());
                            throw new OAuthEmailConflictException();
                        });

        if (userOauthConnectionRepository.existsByEmailAndUser_IdNot(
                profile.email(), user.getId())) {
            log.warn(
                    "OAuth link blocked by existing connection email conflict. provider={},"
                            + " userId={}",
                    profile.provider(),
                    user.getId());
            throw new OAuthEmailConflictException();
        }

        var oauthConnection =
                UserOauthConnection.builder()
                        .user(user)
                        .provider(profile.provider())
                        .providerUserId(profile.providerUserId())
                        .email(profile.email())
                        .build();

        userOauthConnectionRepository.save(oauthConnection);
        log.info(
                "OAuth connection linked. provider={}, userId={}",
                profile.provider(),
                user.getId());
    }
}
