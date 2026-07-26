package com.github.mohrezal.identity.domain.auth.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.mohrezal.identity.domain.auth.command.param.UnlinkOAuthConnectionCommandParams;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthCannotUnlinkLastLoginMethodException;
import com.github.mohrezal.identity.domain.auth.model.UserOauthConnection;
import com.github.mohrezal.identity.domain.auth.repository.UserOauthConnectionRepository;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserCredentialRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UnlinkOAuthConnectionCommandTest {

    @Mock
    private UserOauthConnectionRepository userOauthConnectionRepository;

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @InjectMocks
    private UnlinkOAuthConnectionCommand command;

    @Test
    void execute_whenConnectionIsOnlyLoginMethod_rejectsWithoutDeleting() {
        var user = User.builder().id(UUID.randomUUID()).email("user@client.test").build();
        var connectionId = UUID.randomUUID();
        var connection =
                UserOauthConnection.builder()
                        .id(connectionId)
                        .user(user)
                        .provider(OAuthProviderType.GOOGLE)
                        .build();
        var params = new UnlinkOAuthConnectionCommandParams(user, connectionId);
        when(userOauthConnectionRepository.findByIdAndUser(connectionId, user))
                .thenReturn(Optional.of(connection));
        when(userCredentialRepository.existsByUser(user)).thenReturn(false);
        when(userOauthConnectionRepository.countByUser(user)).thenReturn(1L);

        assertThatThrownBy(() -> command.execute(params, null))
                .isInstanceOf(OAuthCannotUnlinkLastLoginMethodException.class);
        verify(userOauthConnectionRepository, never()).delete(connection);
    }

    @ParameterizedTest
    @CsvSource({"true, 1", "false, 2"})
    void execute_whenAnotherLoginMethodExists_deletesConnection(
            boolean hasPasswordLogin, long oauthConnectionCount) {
        var user = User.builder().id(UUID.randomUUID()).email("user@client.test").build();
        var connectionId = UUID.randomUUID();
        var connection =
                UserOauthConnection.builder()
                        .id(connectionId)
                        .user(user)
                        .provider(OAuthProviderType.GOOGLE)
                        .build();
        var params = new UnlinkOAuthConnectionCommandParams(user, connectionId);
        when(userOauthConnectionRepository.findByIdAndUser(connectionId, user))
                .thenReturn(Optional.of(connection));
        when(userCredentialRepository.existsByUser(user)).thenReturn(hasPasswordLogin);
        when(userOauthConnectionRepository.countByUser(user)).thenReturn(oauthConnectionCount);

        var result = command.execute(params, null);

        assertThat(result).isNull();
        verify(userOauthConnectionRepository).delete(connection);
    }
}
