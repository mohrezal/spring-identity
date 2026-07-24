package com.github.mohrezal.identity.domain.auth.command;

import static com.github.mohrezal.identity.support.data.TestConstants.Redirect.PASSWORD_RESET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.mohrezal.identity.domain.auth.command.param.ForgotPasswordCommandParams;
import com.github.mohrezal.identity.domain.auth.dto.ForgotPasswordRequest;
import com.github.mohrezal.identity.domain.auth.listener.message.PasswordResetEmailMessage;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.shared.service.RedirectValidationService;
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
class ForgotPasswordCommandTest {

    private static final String EMAIL = "user@client.test";

    @Mock
    private RedirectValidationService redirectValidationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ForgotPasswordCommand command;

    @Test
    void execute_whenEmailIsUnknown_returnsSuccessWithoutCreatingResetState() {
        var params =
                new ForgotPasswordCommandParams(new ForgotPasswordRequest(EMAIL), PASSWORD_RESET);
        when(redirectValidationService.isValid(PASSWORD_RESET)).thenReturn(true);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        var result = command.execute(params, null);

        assertThat(result).isTrue();
        verifyNoInteractions(redisService, eventPublisher);
    }

    @Test
    void execute_whenEmailExists_storesTokenAndPublishesMatchingResetUrl() {
        var userId = UUID.randomUUID();
        var user = User.builder().id(userId).email(EMAIL).build();
        var params =
                new ForgotPasswordCommandParams(new ForgotPasswordRequest(EMAIL), PASSWORD_RESET);
        var tokenCaptor = ArgumentCaptor.forClass(String.class);
        var eventCaptor = ArgumentCaptor.forClass(PasswordResetEmailMessage.class);
        when(redirectValidationService.isValid(PASSWORD_RESET)).thenReturn(true);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        var result = command.execute(params, null);

        assertThat(result).isTrue();
        verify(redisService)
                .set(eq(RedisKey.PASSWORD_RESET_TOKEN), eq(EMAIL), tokenCaptor.capture());
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo(userId);
        assertThat(eventCaptor.getValue().to()).isEqualTo(EMAIL);
        assertThat(eventCaptor.getValue().resetUrl())
                .isEqualTo(PASSWORD_RESET + "?token=" + tokenCaptor.getValue());
    }
}
