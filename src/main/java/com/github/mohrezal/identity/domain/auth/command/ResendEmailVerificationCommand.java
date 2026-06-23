package com.github.mohrezal.identity.domain.auth.command;

import com.github.mohrezal.identity.domain.auth.command.param.ResendEmailVerificationCommandParams;
import com.github.mohrezal.identity.domain.user.listener.message.UserEmailVerificationMessage;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.exception.type.InvalidRedirectUrlException;
import com.github.mohrezal.identity.shared.interfaces.Command;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.shared.service.RedirectValidationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class ResendEmailVerificationCommand
        implements Command<ResendEmailVerificationCommandParams, Boolean> {
    private final RedirectValidationService redirectValidationService;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void validate(ResendEmailVerificationCommandParams params) {
        if (!redirectValidationService.isValid(params.redirectUrl())) {
            throw new InvalidRedirectUrlException();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean execute(ResendEmailVerificationCommandParams params) {
        validate(params);

        var optionalUser = userRepository.findByEmail(params.request().email());
        if (optionalUser.isEmpty() || optionalUser.get().getEmailVerifiedAt() != null) return true;

        var token = UUID.randomUUID().toString();
        var email = params.request().email();
        redisService.set(RedisKey.EMAIL_VERIFICATION_TOKEN, email, token);
        var activationUrl =
                UriComponentsBuilder.fromPath(params.redirectUrl())
                        .queryParam("token", token)
                        .toUriString();
        var emailVerificationEvent = new UserEmailVerificationMessage(email, activationUrl);

        eventPublisher.publishEvent(emailVerificationEvent);
        return true;
    }
}
