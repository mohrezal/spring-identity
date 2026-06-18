package com.github.mohrezal.identity.domain.auth.command;

import com.github.mohrezal.identity.domain.auth.command.param.LogoutCommandParams;
import com.github.mohrezal.identity.domain.auth.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.shared.interfaces.Command;
import com.github.mohrezal.identity.shared.service.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutCommand implements Command<LogoutCommandParams, Void> {

    private final RefreshTokenRepository refreshTokenRepository;
    private final HashService hashService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void execute(LogoutCommandParams params) {
        if (!StringUtils.hasText(params.rawRefreshToken())) {
            log.info("Logout requested without refresh token cookie.");
            return null;
        }

        var hashedRefreshToken = hashService.hashHex(params.rawRefreshToken());
        var refreshToken = refreshTokenRepository.findByHashedToken(hashedRefreshToken);

        if (refreshToken.isEmpty()) {
            log.info("Logout requested with unknown refresh token.");
            return null;
        }

        if (refreshToken.get().isActive()) {
            refreshToken.get().revoke();
            refreshTokenRepository.save(refreshToken.get());
            log.info(
                    "Refresh token revoked during logout. refreshTokenId={}, userId={}",
                    refreshToken.get().getId(),
                    refreshToken.get().getUser().getId());
        }

        return null;
    }
}
