package com.github.mohrezal.identity.domain.authentication.command;

import com.github.mohrezal.identity.domain.authentication.command.param.LogoutAllCommandParams;
import com.github.mohrezal.identity.domain.authentication.model.RefreshToken;
import com.github.mohrezal.identity.domain.authentication.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.shared.abstracts.AuthenticatedCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutAllCommand extends AuthenticatedCommand<LogoutAllCommandParams, Void> {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void execute(LogoutAllCommandParams params) {
        var user = getCurrentUser(params);
        var activeRefreshTokens = refreshTokenRepository.findAllByUserAndRevokedAtIsNull(user);

        activeRefreshTokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(activeRefreshTokens);

        log.info(
                "Revoked all active refresh tokens during logout-all. userId={}, count={}",
                user.getId(),
                activeRefreshTokens.size());

        return null;
    }
}
