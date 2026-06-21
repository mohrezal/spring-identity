package com.github.mohrezal.identity.domain.authentication.command;

import com.github.mohrezal.identity.domain.authentication.command.param.ResetPasswordCommandParams;
import com.github.mohrezal.identity.domain.authentication.exception.type.AuthPasswordResetTokenNotFoundException;
import com.github.mohrezal.identity.domain.authentication.model.RefreshToken;
import com.github.mohrezal.identity.domain.authentication.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.domain.user.exception.type.UserNotFoundException;
import com.github.mohrezal.identity.domain.user.model.UserCredential;
import com.github.mohrezal.identity.domain.user.repository.UserCredentialRepository;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.enums.RedisKey;
import com.github.mohrezal.identity.shared.exception.type.InvalidRedirectUrlException;
import com.github.mohrezal.identity.shared.interfaces.Command;
import com.github.mohrezal.identity.shared.redis.RedisService;
import com.github.mohrezal.identity.shared.service.RedirectValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordCommand implements Command<ResetPasswordCommandParams, Void> {

    private final RedisService redisService;
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedirectValidationService redirectValidationService;

    @Override
    public void validate(ResetPasswordCommandParams params) {
        if (!redirectValidationService.isValid(params.redirectUrl())) {
            throw new InvalidRedirectUrlException();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void execute(ResetPasswordCommandParams params) {
        validate(params);

        var token = params.request().token().toString();
        var email =
                redisService
                        .consume(RedisKey.PASSWORD_RESET_TOKEN, String.class, token)
                        .orElseThrow(AuthPasswordResetTokenNotFoundException::new);

        var user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        var hashedPassword = passwordEncoder.encode(params.request().newPassword());
        var credential = userCredentialRepository.findByUser(user);

        if (credential.isPresent()) {
            credential.get().changePassword(hashedPassword);
            userCredentialRepository.save(credential.get());
        } else {
            userCredentialRepository.save(
                    UserCredential.builder().user(user).hashedPassword(hashedPassword).build());
        }

        var activeRefreshTokens = refreshTokenRepository.findAllByUserAndRevokedAtIsNull(user);
        activeRefreshTokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(activeRefreshTokens);

        log.info(
                "Password reset completed. userId={}, revokedRefreshTokenCount={}",
                user.getId(),
                activeRefreshTokens.size());

        return null;
    }
}
