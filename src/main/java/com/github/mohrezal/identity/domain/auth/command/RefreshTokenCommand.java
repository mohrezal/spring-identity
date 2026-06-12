package com.github.mohrezal.identity.domain.auth.command;

import com.github.mohrezal.identity.domain.auth.command.param.RefreshTokenCommandParams;
import com.github.mohrezal.identity.domain.auth.dto.AuthResponse;
import com.github.mohrezal.identity.domain.auth.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.domain.auth.service.TokenIssuanceService;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import com.github.mohrezal.identity.shared.interfaces.Command;
import com.github.mohrezal.identity.shared.service.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenCommand implements Command<RefreshTokenCommandParams, AuthResponse> {

    private final RefreshTokenRepository refreshTokenRepository;
    private final HashService hashService;
    private final TokenIssuanceService tokenIssuanceService;

    @Override
    public void validate(RefreshTokenCommandParams params) {
        if (params.rawRefreshToken() == null) {
            throw new UnauthorizedException();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AuthResponse execute(RefreshTokenCommandParams params) {
        validate(params);

        var hashedRefreshToken = hashService.sha256(params.rawRefreshToken());
        var refreshToken =
                refreshTokenRepository
                        .findByHashedToken(hashedRefreshToken)
                        .orElseThrow(UnauthorizedException::new);
        if (!refreshToken.isActive()) {
            log.warn(
                    "Refresh token rotation rejected for inactive token. refreshTokenId={},"
                            + " userId={}",
                    refreshToken.getId(),
                    refreshToken.getUser().getId());
            throw new UnauthorizedException();
        }

        log.info(
                "Refresh token rotation accepted. refreshTokenId={}, userId={}",
                refreshToken.getId(),
                refreshToken.getUser().getId());

        return tokenIssuanceService.rotate(refreshToken, params.ipAddress(), params.userAgent());
    }
}
