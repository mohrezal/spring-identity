package com.github.mohrezal.identity.domain.auth.command;

import static com.github.mohrezal.identity.support.data.TestConstants.Account.EMAIL;
import static com.github.mohrezal.identity.support.data.TestConstants.Redirect.EMAIL_VERIFICATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.auth.command.param.VerifyEmailCommandParams;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthEmailAlreadyVerifiedException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthEmailVerificationTokenNotFoundException;
import com.github.mohrezal.identity.domain.user.exception.type.UserNotFoundException;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.exception.type.InvalidRedirectUrlException;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.shared.service.RedirectValidationService;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerifyEmailCommandTest {

    @Mock
    private RedirectValidationService redirectValidationService;

    @Mock
    private RedisService redisService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VerifyEmailCommand command;

    @Test
    void execute_whenTokenValid_returnsUserIdAndEmailAndMarksVerified() {
        var userId = UUID.randomUUID();
        var token = UUID.randomUUID();
        var user = User.builder().id(userId).email(EMAIL).build();
        var params = new VerifyEmailCommandParams(token, EMAIL_VERIFICATION);
        var audit = mock(AuditRequestContext.class);

        when(redirectValidationService.isValid(EMAIL_VERIFICATION)).thenReturn(true);
        when(redisService.get(RedisKey.EMAIL_VERIFICATION_TOKEN, String.class, token.toString()))
                .thenReturn(Optional.of(EMAIL));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        var result = command.execute(params, audit);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(user.getEmailVerifiedAt()).isNotNull();
        verify(redisService).delete(RedisKey.EMAIL_VERIFICATION_TOKEN, token.toString());
    }

    @Test
    void execute_whenTokenMissing_throwsNotFound() {
        var token = UUID.randomUUID();
        var params = new VerifyEmailCommandParams(token, EMAIL_VERIFICATION);
        var audit = mock(AuditRequestContext.class);

        when(redirectValidationService.isValid(EMAIL_VERIFICATION)).thenReturn(true);
        when(redisService.get(RedisKey.EMAIL_VERIFICATION_TOKEN, String.class, token.toString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> command.execute(params, audit))
                .isInstanceOf(AuthEmailVerificationTokenNotFoundException.class);
        verifyNoInteractions(userRepository);
    }

    @Test
    void execute_whenAlreadyVerified_throwsAlreadyVerified() {
        var token = UUID.randomUUID();
        var user =
                User.builder()
                        .id(UUID.randomUUID())
                        .email(EMAIL)
                        .emailVerifiedAt(OffsetDateTime.now())
                        .build();
        var params = new VerifyEmailCommandParams(token, EMAIL_VERIFICATION);
        var audit = mock(AuditRequestContext.class);

        when(redirectValidationService.isValid(EMAIL_VERIFICATION)).thenReturn(true);
        when(redisService.get(RedisKey.EMAIL_VERIFICATION_TOKEN, String.class, token.toString()))
                .thenReturn(Optional.of(EMAIL));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> command.execute(params, audit))
                .isInstanceOf(AuthEmailAlreadyVerifiedException.class);
    }

    @Test
    void execute_whenUserMissing_throwsUserNotFound() {
        var token = UUID.randomUUID();
        var params = new VerifyEmailCommandParams(token, EMAIL_VERIFICATION);
        var audit = mock(AuditRequestContext.class);

        when(redirectValidationService.isValid(EMAIL_VERIFICATION)).thenReturn(true);
        when(redisService.get(RedisKey.EMAIL_VERIFICATION_TOKEN, String.class, token.toString()))
                .thenReturn(Optional.of(EMAIL));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> command.execute(params, audit))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void execute_whenRedirectInvalid_throwsInvalidRedirectUrl() {
        var token = UUID.randomUUID();
        var params = new VerifyEmailCommandParams(token, EMAIL_VERIFICATION);
        var audit = mock(AuditRequestContext.class);

        when(redirectValidationService.isValid(EMAIL_VERIFICATION)).thenReturn(false);

        assertThatThrownBy(() -> command.execute(params, audit))
                .isInstanceOf(InvalidRedirectUrlException.class);
        verifyNoInteractions(redisService, userRepository);
    }
}
