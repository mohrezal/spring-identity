package com.github.mohrezal.identity.domain.auth.command;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.auth.command.param.ChangePasswordCommandParams;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthCurrentPasswordMismatchException;
import com.github.mohrezal.identity.domain.auth.model.RefreshToken;
import com.github.mohrezal.identity.domain.auth.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.domain.user.repository.UserCredentialRepository;
import com.github.mohrezal.identity.shared.abstracts.AuthenticatedCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordCommand extends AuthenticatedCommand<ChangePasswordCommandParams, Void> {

    private final UserCredentialRepository userCredentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void execute(
            ChangePasswordCommandParams params, AuditRequestContext auditRequestContext) {
        validate(params);

        var user = getCurrentUser(params);
        var credential =
                userCredentialRepository
                        .findByUser(user)
                        .orElseThrow(AuthCurrentPasswordMismatchException::new);

        if (!passwordEncoder.matches(
                params.request().currentPassword(), credential.getHashedPassword())) {
            log.warn("Password change rejected. userId={}", user.getId());
            throw new AuthCurrentPasswordMismatchException();
        }

        var hashedNewPassword = passwordEncoder.encode(params.request().newPassword());

        credential.changePassword(hashedNewPassword);
        userCredentialRepository.save(credential);
        log.info("Password changed. userId={}", user.getId());

        var activeRefreshTokens = refreshTokenRepository.findAllByUserAndRevokedAtIsNull(user);
        activeRefreshTokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(activeRefreshTokens);

        log.info(
                "Revoked active refresh tokens after password update. userId={}, count={}",
                user.getId(),
                activeRefreshTokens.size());

        return null;
    }
}
