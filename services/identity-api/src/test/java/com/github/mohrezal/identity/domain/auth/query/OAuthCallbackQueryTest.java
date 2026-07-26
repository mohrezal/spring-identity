package com.github.mohrezal.identity.domain.auth.query;

import static com.github.mohrezal.identity.support.data.TestConstants.Origin.CLIENT;
import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.IP_ADDRESS;
import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.USER_AGENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.mohrezal.identity.domain.auth.dto.AuthResponse;
import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthStatePayload;
import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthUserProfile;
import com.github.mohrezal.identity.domain.auth.enums.OAuthFlowType;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthEmailConflictException;
import com.github.mohrezal.identity.domain.auth.listener.message.OAuthWelcomeEmailMessage;
import com.github.mohrezal.identity.domain.auth.model.UserOauthConnection;
import com.github.mohrezal.identity.domain.auth.query.param.OAuthCallbackQueryParams;
import com.github.mohrezal.identity.domain.auth.repository.UserOauthConnectionRepository;
import com.github.mohrezal.identity.domain.auth.service.TokenIssuanceService;
import com.github.mohrezal.identity.domain.auth.service.oauth.OAuthLinkService;
import com.github.mohrezal.identity.domain.auth.service.oauth.OAuthProvider;
import com.github.mohrezal.identity.domain.auth.service.oauth.OAuthProviderRegistry;
import com.github.mohrezal.identity.domain.privilege.service.UserRoleAssignmentService;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import com.github.mohrezal.identity.shared.redis.RedisService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class OAuthCallbackQueryTest {

    private static final OAuthProviderType PROVIDER = OAuthProviderType.GOOGLE;
    private static final String CODE = "authorization-code";
    private static final String STATE = "oauth-state";
    private static final String CORRELATION_ID = "correlation-id";
    private static final String REDIRECT_URL = CLIENT + "/oauth/callback";
    private static final String PROVIDER_USER_ID = "google-user-123";
    private static final String EMAIL = "oauth-user@client.test";

    @Mock
    private RedisService redisService;

    @Mock
    private OAuthProviderRegistry providerRegistry;

    @Mock
    private OAuthProvider provider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserOauthConnectionRepository userOauthConnectionRepository;

    @Mock
    private TokenIssuanceService tokenIssuanceService;

    @Mock
    private OAuthLinkService oAuthLinkService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserRoleAssignmentService userRoleAssignmentService;

    @InjectMocks
    private OAuthCallbackQuery query;

    @Test
    void execute_whenStateDoesNotExist_rejectsBeforeCallingProvider() {
        var params =
                new OAuthCallbackQueryParams(
                        PROVIDER, CODE, STATE, CORRELATION_ID, IP_ADDRESS, USER_AGENT);
        when(redisService.consume(RedisKey.OAUTH_STATE, OAuthStatePayload.class, STATE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> query.execute(params, null))
                .isInstanceOf(UnauthorizedException.class);
        verifyNoInteractions(providerRegistry, provider);
    }

    @Test
    void execute_whenStateProviderDoesNotMatch_rejectsBeforeCallingProvider() {
        var params =
                new OAuthCallbackQueryParams(
                        PROVIDER, CODE, STATE, CORRELATION_ID, IP_ADDRESS, USER_AGENT);
        var payload =
                new OAuthStatePayload(
                        REDIRECT_URL,
                        OAuthFlowType.LOGIN,
                        OAuthProviderType.TWITTER,
                        null,
                        CORRELATION_ID);
        when(redisService.consume(RedisKey.OAUTH_STATE, OAuthStatePayload.class, STATE))
                .thenReturn(Optional.of(payload));

        assertThatThrownBy(() -> query.execute(params, null))
                .isInstanceOf(UnauthorizedException.class);
        verifyNoInteractions(providerRegistry, provider);
    }

    @Test
    void execute_whenCorrelationIdDoesNotMatch_rejectsBeforeCallingProvider() {
        var params =
                new OAuthCallbackQueryParams(
                        PROVIDER, CODE, STATE, "wrong-correlation", IP_ADDRESS, USER_AGENT);
        var payload =
                new OAuthStatePayload(
                        REDIRECT_URL, OAuthFlowType.LOGIN, PROVIDER, null, CORRELATION_ID);
        when(redisService.consume(RedisKey.OAUTH_STATE, OAuthStatePayload.class, STATE))
                .thenReturn(Optional.of(payload));

        assertThatThrownBy(() -> query.execute(params, null))
                .isInstanceOf(UnauthorizedException.class);
        verifyNoInteractions(providerRegistry, provider);
    }

    @Test
    void execute_whenProviderProfileIsUnverified_rejectsBeforeAccessingUsers() {
        var params =
                new OAuthCallbackQueryParams(
                        PROVIDER, CODE, STATE, CORRELATION_ID, IP_ADDRESS, USER_AGENT);
        var payload =
                new OAuthStatePayload(
                        REDIRECT_URL, OAuthFlowType.LOGIN, PROVIDER, null, CORRELATION_ID);
        var profile =
                new OAuthUserProfile(PROVIDER_USER_ID, EMAIL, false, "OAuth", "User", PROVIDER);
        when(redisService.consume(RedisKey.OAUTH_STATE, OAuthStatePayload.class, STATE))
                .thenReturn(Optional.of(payload));
        when(providerRegistry.get(PROVIDER)).thenReturn(provider);
        when(provider.profile(CODE)).thenReturn(profile);

        assertThatThrownBy(() -> query.execute(params, null))
                .isInstanceOf(UnauthorizedException.class);
        verifyNoInteractions(userRepository, userOauthConnectionRepository);
    }

    @Test
    void execute_whenConnectionExists_issuesTokensForExistingUser() {
        var user = User.builder().id(UUID.randomUUID()).email(EMAIL).build();
        var connection = UserOauthConnection.builder().user(user).build();
        var authResponse = new AuthResponse("access-token", "refresh-token");
        var params =
                new OAuthCallbackQueryParams(
                        PROVIDER, CODE, STATE, CORRELATION_ID, IP_ADDRESS, USER_AGENT);
        var payload =
                new OAuthStatePayload(
                        REDIRECT_URL, OAuthFlowType.LOGIN, PROVIDER, null, CORRELATION_ID);
        var profile =
                new OAuthUserProfile(PROVIDER_USER_ID, EMAIL, true, "OAuth", "User", PROVIDER);
        when(redisService.consume(RedisKey.OAUTH_STATE, OAuthStatePayload.class, STATE))
                .thenReturn(Optional.of(payload));
        when(providerRegistry.get(PROVIDER)).thenReturn(provider);
        when(provider.profile(CODE)).thenReturn(profile);
        when(userOauthConnectionRepository.findByProviderAndProviderUserId(
                        PROVIDER, PROVIDER_USER_ID))
                .thenReturn(Optional.of(connection));
        when(tokenIssuanceService.issue(user, IP_ADDRESS, USER_AGENT)).thenReturn(authResponse);

        var response = query.execute(params, null);

        assertThat(response.authResponse()).isSameAs(authResponse);
        assertThat(response.redirectUrl()).isEqualTo(REDIRECT_URL);
        assertThat(response.flowType()).isEqualTo(OAuthFlowType.LOGIN);
        verify(tokenIssuanceService).issue(user, IP_ADDRESS, USER_AGENT);
        verifyNoInteractions(userRepository, userRoleAssignmentService, eventPublisher);
    }

    @Test
    void execute_whenLocalEmailExists_rejectsImplicitAccountLinking() {
        var params =
                new OAuthCallbackQueryParams(
                        PROVIDER, CODE, STATE, CORRELATION_ID, IP_ADDRESS, USER_AGENT);
        var payload =
                new OAuthStatePayload(
                        REDIRECT_URL, OAuthFlowType.LOGIN, PROVIDER, null, CORRELATION_ID);
        var profile =
                new OAuthUserProfile(PROVIDER_USER_ID, EMAIL, true, "OAuth", "User", PROVIDER);
        when(redisService.consume(RedisKey.OAUTH_STATE, OAuthStatePayload.class, STATE))
                .thenReturn(Optional.of(payload));
        when(providerRegistry.get(PROVIDER)).thenReturn(provider);
        when(provider.profile(CODE)).thenReturn(profile);
        when(userOauthConnectionRepository.findByProviderAndProviderUserId(
                        PROVIDER, PROVIDER_USER_ID))
                .thenReturn(Optional.empty());
        when(userRepository.existsUserByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> query.execute(params, null))
                .isInstanceOf(OAuthEmailConflictException.class);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(tokenIssuanceService, userRoleAssignmentService, eventPublisher);
    }

    @Test
    void execute_whenProviderIdentityIsNew_createsVerifiedUserAndConnection() {
        var userId = UUID.randomUUID();
        var savedUser = User.builder().id(userId).email(EMAIL).build();
        var authResponse = new AuthResponse("access-token", "refresh-token");
        var userCaptor = ArgumentCaptor.forClass(User.class);
        var connectionCaptor = ArgumentCaptor.forClass(UserOauthConnection.class);
        var eventCaptor = ArgumentCaptor.forClass(OAuthWelcomeEmailMessage.class);
        var params =
                new OAuthCallbackQueryParams(
                        PROVIDER, CODE, STATE, CORRELATION_ID, IP_ADDRESS, USER_AGENT);
        var payload =
                new OAuthStatePayload(
                        REDIRECT_URL, OAuthFlowType.LOGIN, PROVIDER, null, CORRELATION_ID);
        var profile =
                new OAuthUserProfile(PROVIDER_USER_ID, EMAIL, true, "OAuth", "User", PROVIDER);
        when(redisService.consume(RedisKey.OAUTH_STATE, OAuthStatePayload.class, STATE))
                .thenReturn(Optional.of(payload));
        when(providerRegistry.get(PROVIDER)).thenReturn(provider);
        when(provider.profile(CODE)).thenReturn(profile);
        when(userOauthConnectionRepository.findByProviderAndProviderUserId(
                        PROVIDER, PROVIDER_USER_ID))
                .thenReturn(Optional.empty());
        when(userRepository.existsUserByEmail(EMAIL)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenIssuanceService.issue(savedUser, IP_ADDRESS, USER_AGENT))
                .thenReturn(authResponse);

        var response = query.execute(params, null);

        assertThat(response.authResponse()).isSameAs(authResponse);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue())
                .extracting(User::getEmail, User::getFirstName, User::getLastName)
                .containsExactly(EMAIL, "OAuth", "User");
        assertThat(userCaptor.getValue().getEmailVerifiedAt()).isNotNull();
        verify(userRoleAssignmentService).assignConfiguredUserRole(savedUser);
        verify(userOauthConnectionRepository).save(connectionCaptor.capture());
        assertThat(connectionCaptor.getValue())
                .extracting(
                        UserOauthConnection::getUser,
                        UserOauthConnection::getProvider,
                        UserOauthConnection::getProviderUserId,
                        UserOauthConnection::getEmail)
                .containsExactly(savedUser, PROVIDER, PROVIDER_USER_ID, EMAIL);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue())
                .isEqualTo(new OAuthWelcomeEmailMessage(userId, EMAIL, PROVIDER));
        verify(tokenIssuanceService).issue(savedUser, IP_ADDRESS, USER_AGENT);
    }

    @Test
    void execute_whenFlowIsLink_delegatesUsingUserCapturedInState() {
        var userId = UUID.randomUUID();
        var params =
                new OAuthCallbackQueryParams(
                        PROVIDER, CODE, STATE, CORRELATION_ID, IP_ADDRESS, USER_AGENT);
        var payload =
                new OAuthStatePayload(
                        REDIRECT_URL, OAuthFlowType.LINK, PROVIDER, userId, CORRELATION_ID);
        var profile =
                new OAuthUserProfile(PROVIDER_USER_ID, EMAIL, true, "OAuth", "User", PROVIDER);
        when(redisService.consume(RedisKey.OAUTH_STATE, OAuthStatePayload.class, STATE))
                .thenReturn(Optional.of(payload));
        when(providerRegistry.get(PROVIDER)).thenReturn(provider);
        when(provider.profile(CODE)).thenReturn(profile);

        var response = query.execute(params, null);

        assertThat(response.authResponse()).isNull();
        assertThat(response.redirectUrl()).isEqualTo(REDIRECT_URL);
        assertThat(response.flowType()).isEqualTo(OAuthFlowType.LINK);
        verify(oAuthLinkService).link(userId, PROVIDER, PROVIDER_USER_ID, EMAIL);
        verifyNoInteractions(tokenIssuanceService);
    }
}
