package com.github.mohrezal.identity.domain.auth.command;

import com.github.mohrezal.identity.domain.auth.command.param.VerifyEmailCommandParams;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthEmailAlreadyVerifiedException;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthEmailVerificationTokenNotFoundException;
import com.github.mohrezal.identity.domain.user.exception.type.UserNotFoundException;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.exception.type.InvalidRedirectUrlException;
import com.github.mohrezal.identity.shared.interfaces.Command;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.shared.service.RedirectValidationService;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerifyEmailCommand implements Command<VerifyEmailCommandParams, Void> {

    private final RedirectValidationService redirectValidationService;
    private final RedisService redisService;
    private final UserRepository userRepository;

    @Override
    public void validate(VerifyEmailCommandParams params) {
        if (!redirectValidationService.isValid(params.redirectUrl())) {
            throw new InvalidRedirectUrlException();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Void execute(VerifyEmailCommandParams params) {
        validate(params);
        var email =
                redisService
                        .get(
                                RedisKey.EMAIL_VERIFICATION_TOKEN,
                                String.class,
                                params.token().toString())
                        .orElseThrow(AuthEmailVerificationTokenNotFoundException::new);
        var user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        if (user.getEmailVerifiedAt() != null) {
            throw new AuthEmailAlreadyVerifiedException();
        }
        user.setEmailVerifiedAt(OffsetDateTime.now());
        userRepository.save(user);
        redisService.delete(RedisKey.EMAIL_VERIFICATION_TOKEN, params.token().toString());
        return null;
    }
}
