package com.github.mohrezal.identity.domain.auth.service;

import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.IP_ADDRESS;
import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.OTHER_IP_ADDRESS;
import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.OTHER_USER_AGENT;
import static com.github.mohrezal.identity.support.data.TestConstants.RequestMetadata.USER_AGENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.mohrezal.identity.config.security.JwtTokenProvider;
import com.github.mohrezal.identity.domain.auth.model.RefreshToken;
import com.github.mohrezal.identity.domain.auth.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.domain.privilege.service.UserPermissionService;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import com.github.mohrezal.identity.shared.service.HashService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenIssuanceServiceTest {

    private static final String ACCESS_TOKEN = "access.jwt";
    private static final String REFRESH_TOKEN = "refresh.jwt";
    private static final String HASHED_REFRESH_TOKEN = "hashed-refresh-token";

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private HashService hashService;

    @Mock
    private UserPermissionService userPermissionService;

    @InjectMocks
    private TokenIssuanceService service;

    @Nested
    class Issue {

        @Test
        void whenTokensAreCreated_persistsOnlyRefreshTokenHashAndMetadata() {
            var userId = UUID.randomUUID();
            var user = user(userId);
            var permissions = List.of(Permissions.IDENTITY_AUTH_SESSIONS_READ);
            var expiration = OffsetDateTime.now().plusDays(14);
            var refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

            when(userPermissionService.getPermissionKeys(userId)).thenReturn(permissions);
            when(jwtTokenProvider.createAccessToken(userId, permissions, 0L))
                    .thenReturn(ACCESS_TOKEN);
            when(jwtTokenProvider.createRefreshToken(userId)).thenReturn(REFRESH_TOKEN);
            when(hashService.hashHex(REFRESH_TOKEN)).thenReturn(HASHED_REFRESH_TOKEN);
            when(jwtTokenProvider.extractExpiration(REFRESH_TOKEN))
                    .thenReturn(Optional.of(expiration));
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var response = service.issue(user, IP_ADDRESS, USER_AGENT);

            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
            verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
            assertThat(refreshTokenCaptor.getValue())
                    .extracting(
                            RefreshToken::getUser,
                            RefreshToken::getHashedToken,
                            RefreshToken::getIpAddress,
                            RefreshToken::getDeviceInfo,
                            RefreshToken::getExpiresAt)
                    .containsExactly(
                            user, HASHED_REFRESH_TOKEN, IP_ADDRESS, USER_AGENT, expiration);
            assertThat(refreshTokenCaptor.getValue().getHashedToken()).isNotEqualTo(REFRESH_TOKEN);
        }

        @Test
        void whenRefreshExpirationCannotBeRead_doesNotPersistSession() {
            var userId = UUID.randomUUID();
            var user = user(userId);
            var permissions = List.of(Permissions.IDENTITY_AUTH_SESSIONS_READ);

            when(userPermissionService.getPermissionKeys(userId)).thenReturn(permissions);
            when(jwtTokenProvider.createAccessToken(userId, permissions, 0L))
                    .thenReturn(ACCESS_TOKEN);
            when(jwtTokenProvider.createRefreshToken(userId)).thenReturn(REFRESH_TOKEN);
            when(hashService.hashHex(REFRESH_TOKEN)).thenReturn(HASHED_REFRESH_TOKEN);
            when(jwtTokenProvider.extractExpiration(REFRESH_TOKEN)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.issue(user, IP_ADDRESS, USER_AGENT))
                    .isInstanceOf(UnauthorizedException.class);
            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    class Rotate {

        @Test
        void whenTokenIsRotated_revokesOldSessionBeforeIssuingReplacement() {
            var userId = UUID.randomUUID();
            var user = user(userId);
            var permissions = List.of(Permissions.IDENTITY_AUTH_SESSIONS_READ);
            var oldSession =
                    RefreshToken.builder()
                            .id(UUID.randomUUID())
                            .user(user)
                            .hashedToken("old-hash")
                            .expiresAt(OffsetDateTime.now().plusDays(1))
                            .build();

            when(userPermissionService.getPermissionKeys(userId)).thenReturn(permissions);
            when(jwtTokenProvider.createAccessToken(userId, permissions, 0L))
                    .thenReturn(ACCESS_TOKEN);
            when(jwtTokenProvider.createRefreshToken(userId)).thenReturn(REFRESH_TOKEN);
            when(hashService.hashHex(REFRESH_TOKEN)).thenReturn(HASHED_REFRESH_TOKEN);
            when(jwtTokenProvider.extractExpiration(REFRESH_TOKEN))
                    .thenReturn(Optional.of(OffsetDateTime.now().plusDays(14)));
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var response = service.rotate(oldSession, OTHER_IP_ADDRESS, OTHER_USER_AGENT);

            assertThat(oldSession.isRevoked()).isTrue();
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
            var ordered = inOrder(refreshTokenRepository, jwtTokenProvider);
            ordered.verify(refreshTokenRepository).save(oldSession);
            ordered.verify(jwtTokenProvider).createAccessToken(userId, permissions, 0L);
            ordered.verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    private static User user(UUID id) {
        return User.builder().id(id).email("user@client.test").build();
    }
}
