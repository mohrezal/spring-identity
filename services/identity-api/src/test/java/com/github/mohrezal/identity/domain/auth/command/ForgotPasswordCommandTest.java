package com.github.mohrezal.identity.domain.auth.command;

import static com.github.mohrezal.identity.support.data.TestConstants.Redirect.PASSWORD_RESET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.domain.auth.command.param.ForgotPasswordCommandParams;
import com.github.mohrezal.identity.domain.auth.dto.ForgotPasswordRequest;
import com.github.mohrezal.identity.domain.auth.listener.message.PasswordResetEmailMessage;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.shared.service.RedirectValidationService;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
    private static final Duration PASSWORD_RESET_TOKEN_TTL = Duration.ofMinutes(15);

    @Mock
    private RedirectValidationService redirectValidationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private ForgotPasswordCommand command;

    @BeforeEach
    void setUp() {
        lenient()
                .when(applicationProperties.security())
                .thenReturn(
                        new ApplicationProperties.Security(
                                "test-signing-secret-with-more-than-thirty-two-bytes",
                                Duration.ofMinutes(30),
                                PASSWORD_RESET_TOKEN_TTL,
                                List.of("https://client.test"),
                                null,
                                null));
    }

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
                .set(
                        eq(RedisKey.PASSWORD_RESET_TOKEN),
                        eq(EMAIL),
                        eq(PASSWORD_RESET_TOKEN_TTL),
                        tokenCaptor.capture());
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo(userId);
        assertThat(eventCaptor.getValue().to()).isEqualTo(EMAIL);
        assertThat(eventCaptor.getValue().resetUrl())
                .isEqualTo(PASSWORD_RESET + "?token=" + tokenCaptor.getValue());
    }
}
