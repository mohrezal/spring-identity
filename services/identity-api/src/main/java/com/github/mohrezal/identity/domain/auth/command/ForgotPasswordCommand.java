package com.github.mohrezal.identity.domain.auth.command;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.auth.command.param.ForgotPasswordCommandParams;
import com.github.mohrezal.identity.domain.auth.listener.message.PasswordResetEmailMessage;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.exception.type.InvalidRedirectUrlException;
import com.github.mohrezal.identity.shared.interfaces.Command;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.shared.service.RedirectValidationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordCommand implements Command<ForgotPasswordCommandParams, Boolean> {

    private final RedirectValidationService redirectValidationService;
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void validate(ForgotPasswordCommandParams params) {
        if (!redirectValidationService.isValid(params.redirectUrl())) {
            throw new InvalidRedirectUrlException();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(
            ForgotPasswordCommandParams params, AuditRequestContext auditRequestContext) {
        validate(params);

        var user = userRepository.findByEmail(params.request().email());
        if (user.isEmpty()) {
            log.info("Password reset requested for unknown email.");
            return true;
        }

        var token = UUID.randomUUID().toString();
        redisService.set(RedisKey.PASSWORD_RESET_TOKEN, user.get().getEmail(), token);

        var resetUrl =
                UriComponentsBuilder.fromUriString(params.redirectUrl())
                        .queryParam("token", token)
                        .toUriString();

        eventPublisher.publishEvent(
                new PasswordResetEmailMessage(user.get().getId(), user.get().getEmail(), resetUrl));

        log.info("Password reset requested. userId={}", user.get().getId());
        return true;
    }
}
